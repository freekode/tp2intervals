package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Candidate
import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.MissingLinkRepair
import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Reason
import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Status
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository.CalendarIdentity
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository.Accounts
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository.Approval
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsCalendarEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.beans.factory.annotation.Value
import java.security.MessageDigest
import java.util.HexFormat
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDate

@Service
class TrainingPeaksCalendarSync(
    private val source: TrainingPeaksWorkoutRepository,
    private val destination: IntervalsWorkoutRepository,
    private val ledger: TrainingPeaksSyncLedger,
    private val baselines: TrainingPeaksLegacyBaselineRepository,
    private val objectMapper: ObjectMapper,
    private val mode: PreviewOnly,
    @Value("\${app.training-peaks.scheduled-forward-sync-enabled:false}")
    private val scheduledForwardEnabled: Boolean,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun copyScheduled(request: CopyFromCalendarToCalendarRequest, today: LocalDate): ScheduledCopyOutcome {
        val blocked = when {
            mode.enabled -> "PREVIEW_ONLY"
            !scheduledForwardEnabled -> "FORWARD_SYNC_PAUSED"
            !request.skipSynced || request.sourcePlatform != Platform.TRAINING_PEAKS ||
                request.targetPlatform != Platform.INTERVALS -> "UNSAFE_FORWARD_REQUEST"
            baselines.latest()?.state != "APPROVED" -> "BASELINE_REQUIRED"
            request.startDate != today || request.endDate !in today..today.plusDays(1) -> "INVALID_ROLLING_WINDOW"
            else -> null
        }
        if (blocked != null) return ScheduledCopyOutcome(blockedReason = blocked)

        val generation = baselines.generation()
        val user = source.calendarUser()
        if (!user.isAthlete) return ScheduledCopyOutcome(blockedReason = "ATHLETE_REQUIRED")
        val target = destination.resolveCalendarIdentity()
        val accounts = Accounts(user.userId, target.athleteId, generation)
        val approval = baselines.usableApproval(accounts)
            ?: return ScheduledCopyOutcome(blockedReason = "BASELINE_REQUIRED")

        return ScheduledCopyOutcome(result = copy(request, user.userId, target, approval))
    }

    fun copy(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        mode.requireWritable()
        if (!request.skipSynced) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "SKIP_SYNCED_REQUIRED_FOR_FORWARD_COPY")
        }
        return copy(request, source.calendarUser().userId, destination.resolveCalendarIdentity(), null)
    }

    private fun copy(
        request: CopyFromCalendarToCalendarRequest,
        sourceAccount: String,
        target: CalendarIdentity,
        approval: Approval?,
    ): CopyWorkoutsResponse {
        check(!TransactionSynchronizationManager.isActualTransactionActive()) {
            "TrainingPeaks sync must commit each claim before calling Intervals.icu"
        }

        val athlete = target.athleteId
        val previousKeys = legacyDestinationIds(sourceAccount, target)
        val (workouts, receivedCount) = if (approval == null) {
            val calendar = source.getWorkoutsFromCalendar(sourceAccount, request.startDate, request.endDate)
            calendar to calendar.size
        } else {
            val planned = source.getPlannedCalendarEntries(sourceAccount, request.startDate, request.endDate)
            val eligible = planned.filter {
                it.workoutDay.toLocalDate() in request.startDate..request.endDate &&
                    it.getWorkoutType() in request.types && !it.id.isBlank()
            }.map { source.toCalendarWorkout(sourceAccount, it) }
            eligible to planned.size
        }
        val reviews = mutableListOf<WorkoutSyncReview>()
        val legacyMatches by lazy {
            findCandidates(sourceAccount, athlete, request.startDate, request.endDate)
        }
        val plannedWorkouts by lazy { destination.getWorkoutsFromCalendar(request.startDate, request.endDate) }

        var copied = 0
        for (workout in workouts.filter { it.details.type in request.types }) {
            val sourceId = workout.details.externalData.trainingPeaksId
            if (workout.details.type == TrainingType.NOTE) {
                if (approval == null && workout !in plannedWorkouts) {
                    destination.saveWorkoutsToCalendar(listOf(workout))
                    copied++
                }
                continue
            }

            if (sourceId.isNullOrBlank()) continue
            val key = Key(sourceAccount, athlete, sourceId)
            val existing = ledger.find(key)
            if (existing != null) {
                reviewKnown(key, existing, { legacyMatches[sourceId].orEmpty().map { it.id } })?.let(reviews::add)
                continue
            }

            if (sourceId in previousKeys || baselines.isHeld(key)) {
                reviews.add(review(key, WorkoutSyncReview.Reason.LEGACY_BASELINE_HOLD))
                continue
            }
            val candidates = legacyMatches[sourceId].orEmpty().map { it.id }
            when {
                candidates.size > 1 -> {
                    ledger.recordAmbiguousMatch(key)
                    reviews.add(review(key, WorkoutSyncReview.Reason.AMBIGUOUS_IMPORT))
                }
                candidates.size == 1 -> {
                    val eventId = candidates.single()
                    if (verifiedEvent(key, eventId) != null) {
                        if (!ledger.recordLegacyMatch(key, eventId)) {
                            reviewAfterConflict(key)?.let(reviews::add)
                        }
                    } else {
                        ledger.recordAmbiguousMatch(key)
                        reviews.add(review(key, WorkoutSyncReview.Reason.LEGACY_LINK_UNVERIFIED, eventId))
                    }
                }
                ledger.claimForSync(key, approval) -> {
                    // If approval was revoked after reserving the attempt, keep the claim
                    // and avoid sending it. The explicit header pins the verified target.
                    if (approval != null && !baselines.isCurrent(approval)) continue
                    try {
                        val eventId = destination.createScopedCalendarEvent(target, sourceAccount, workout)
                        ledger.confirm(key, eventId)
                        copied++
                    } catch (failure: RuntimeException) {
                        review(key, WorkoutSyncReview.Reason.UNRESOLVED_WRITE)
                        throw failure
                    }
                }
                else -> ledger.find(key)?.let {
                    reviewKnown(key, it) { emptyList() }?.let(reviews::add)
                }
            }
        }

        return CopyWorkoutsResponse(copied, receivedCount - copied, request.startDate, request.endDate, ExternalData.empty(), reviews)
    }

    private data class Inventory(
        val accounts: Accounts,
        val previousId: Long,
        val inventoryVersion: Long,
        val rows: List<TrainingPeaksSyncPreview>,
        val revision: String,
        val approval: Approval?,
        val sourceEntries: List<TPWorkoutCalendarResponseDTO>,
    )

    fun preview(request: TrainingPeaksPreviewRequest, today: LocalDate = LocalDate.now()): TrainingPeaksPreviewInventory {
        val inventory = inventory(request, today)
        return TrainingPeaksPreviewInventory(
            inventory.revision, inventory.accounts.source, inventory.accounts.destination,
            inventory.rows, inventory.approval?.approvedAt, mode.enabled, scheduledForwardEnabled,
        )
    }

    fun approve(request: TrainingPeaksBaselineApprovalRequest, today: LocalDate = LocalDate.now()): TrainingPeaksBaselineApproval {
        mode.requireWritable()
        if (!request.acknowledgeInconclusive) throw InvalidTrainingPeaksPreview("ACKNOWLEDGEMENT_REQUIRED")
        // Make a distinct, reviewable cutover step before starting unattended work.
        if (scheduledForwardEnabled) throw InvalidTrainingPeaksPreview("PAUSE_SCHEDULING_BEFORE_APPROVAL")
        val inventory = inventory(request.selection, today)
        if (request.revision != inventory.revision) throw StaleTrainingPeaksInventory()

        val selectable = inventory.rows.filter {
            it.status == Status.SINGLE_LEGACY_CANDIDATE && it.sourceWorkoutId != null
        }.associate { it.sourceWorkoutId!! to it.candidates.single().eventId }
        if (request.legacyLinks.any { (sourceId, eventId) -> selectable[sourceId] != eventId }) {
            throw InvalidTrainingPeaksPreview("LEGACY_LINK_NOT_IN_REVIEW")
        }

        val holds = inventory.rows.mapNotNull { row ->
            val id = row.sourceWorkoutId ?: return@mapNotNull null
            if (row.status in setOf(Status.VERIFIED_LINK, Status.UNRESOLVED_CLAIM) || id in request.legacyLinks) {
                null
            } else id to row.reason.name
        }.toMap()
        val approved = previewStage(PreviewStage.APPROVAL_PERSIST) {
            baselines.approve(
                inventory.accounts, inventory.previousId, inventory.inventoryVersion, inventory.revision,
                request.selection.startDate, request.selection.endDate,
                objectMapper.writeValueAsString(mapOf(
                    "acknowledgedInconclusive" to true,
                    "types" to request.selection.types,
                    "workouts" to inventory.rows,
                    "selectedLegacyLinks" to request.legacyLinks,
                    "holds" to holds,
                )), request.legacyLinks, holds,
            )
        } ?: throw StaleTrainingPeaksInventory()
        return TrainingPeaksBaselineApproval(approved.revision, approved.approvedAt, holds.size)
    }

    fun resolveUnmatchedHold(
        request: TrainingPeaksHoldResolutionRequest, today: LocalDate = LocalDate.now(),
    ): TrainingPeaksHoldResolution {
        mode.requireWritable()
        if (scheduledForwardEnabled) throw InvalidTrainingPeaksPreview("PAUSE_SCHEDULING_BEFORE_RESOLUTION")
        if (!request.acknowledgeManualVerification) throw InvalidTrainingPeaksPreview("ACKNOWLEDGEMENT_REQUIRED")
        check(!TransactionSynchronizationManager.isActualTransactionActive()) {
            "The hold claim must commit before calling Intervals.icu"
        }

        val inventory = inventory(request.selection, today)
        if (request.revision != inventory.revision) throw StaleTrainingPeaksInventory()
        val approval = inventory.approval ?: throw InvalidTrainingPeaksPreview("BASELINE_REQUIRED")
        val id = request.sourceWorkoutId
        if (inventory.rows.none { it.sourceWorkoutId == id && it.canResolveUnmatchedHold }) {
            throw InvalidTrainingPeaksPreview("HOLD_NOT_ELIGIBLE")
        }
        val entry = inventory.sourceEntries.single { it.id == id }
        val accounts = inventory.accounts
        val workout = previewStage(PreviewStage.SOURCE_ROWS, PreviewProvider.TRAINING_PEAKS) {
            source.toCalendarWorkout(accounts.source, entry)
        }
        val user = previewStage(PreviewStage.TRAINING_PEAKS_ACCOUNT, PreviewProvider.TRAINING_PEAKS) { source.calendarUser() }
        val target = previewStage(PreviewStage.INTERVALS_ACCOUNT, PreviewProvider.INTERVALS) {
            destination.resolveCalendarIdentity()
        }
        if (!user.isAthlete || user.userId != accounts.source || target.athleteId != accounts.destination ||
            (!user.isPremium && entry.workoutDay.toLocalDate() !in today..today.plusDays(1))
        ) throw StaleTrainingPeaksInventory()

        val key = Key(accounts.source, accounts.destination, id)
        val candidates = previewStage(PreviewStage.INTERVALS_CALENDAR_LEGACY_WINDOW, PreviewProvider.INTERVALS) {
            findCandidates(accounts.source, accounts.destination, request.selection.startDate, request.selection.endDate)
        }
        if (id in candidates || id in legacyDestinationIds(accounts.source, target) ||
            !ledger.claimReviewedUnmatchedHold(key, approval, inventory.inventoryVersion) ||
            !baselines.isCurrent(approval)
        ) throw StaleTrainingPeaksInventory()

        return try {
            val eventId = destination.createScopedCalendarEvent(target, accounts.source, workout)
            ledger.confirm(key, eventId)
            check(ledger.find(key)?.remoteEventId == eventId)
            TrainingPeaksHoldResolution(eventId)
        } catch (failure: RuntimeException) {
            log.warn("TrainingPeaks hold resolution needs review: code=HOLD_WRITE_UNCERTAIN exception={}", failure.javaClass.simpleName)
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "HOLD_WRITE_UNCERTAIN")
        }
    }

    fun repairMissingLink(
        request: TrainingPeaksLinkRepairRequest, today: LocalDate = LocalDate.now(),
    ): TrainingPeaksLinkRepair {
        mode.requireWritable()
        if (scheduledForwardEnabled) throw InvalidTrainingPeaksPreview("PAUSE_SCHEDULING_BEFORE_REPAIR")
        if (!request.acknowledgeRepair ||
            (request.replacementEventId == null && !request.acknowledgeInconclusive)
        ) throw InvalidTrainingPeaksPreview("ACKNOWLEDGEMENT_REQUIRED")
        check(!TransactionSynchronizationManager.isActualTransactionActive()) {
            "The repair claim must commit before calling Intervals.icu"
        }

        val reviewed = inventory(request.selection, today)
        if (request.revision != reviewed.revision) throw StaleTrainingPeaksInventory()
        val approval = reviewed.approval ?: throw InvalidTrainingPeaksPreview("BASELINE_REQUIRED")
        val id = request.sourceWorkoutId
        val option = reviewed.rows.singleOrNull { it.sourceWorkoutId == id }?.missingLinkRepair
            ?: throw InvalidTrainingPeaksPreview("REPAIR_NOT_ELIGIBLE")
        if (option.oldEventId != request.oldEventId || option.replacement?.eventId != request.replacementEventId) {
            throw StaleTrainingPeaksInventory()
        }

        val account = reviewed.accounts
        val entry = reviewed.sourceEntries.single { it.id == id }
        val workout = if (option.replacement == null) previewStage(PreviewStage.SOURCE_ROWS, PreviewProvider.TRAINING_PEAKS) {
            source.toCalendarWorkout(account.source, entry)
        } else null
        val user = previewStage(PreviewStage.TRAINING_PEAKS_ACCOUNT, PreviewProvider.TRAINING_PEAKS) { source.calendarUser() }
        val target = previewStage(PreviewStage.INTERVALS_ACCOUNT, PreviewProvider.INTERVALS) { destination.resolveCalendarIdentity() }
        if (!user.isAthlete || user.userId != account.source || target.athleteId != account.destination ||
            (!user.isPremium && entry.workoutDay.toLocalDate() !in today..today.plusDays(1)) ||
            id in legacyDestinationIds(account.source, target)
        ) throw StaleTrainingPeaksInventory()

        val key = Key(account.source, account.destination, id)
        val (start, end) = request.selection
        val current = previewStage(PreviewStage.INTERVALS_CALENDAR_RANGE, PreviewProvider.INTERVALS) {
            destination.getCalendarSnapshot(account.destination, start, end)
        }
        val surrounding = previewStage(PreviewStage.INTERVALS_CALENDAR_LEGACY_WINDOW, PreviewProvider.INTERVALS) {
            destination.getCalendarSnapshot(account.destination, start.minusDays(365), end.plusDays(365))
        }
        val oldEvent = previewStage(PreviewStage.INTERVALS_EVENT_LOOKUP, PreviewProvider.INTERVALS) {
            destination.getCalendarEvent(account.destination, option.oldEventId)
        }
        if (oldEvent != null || repairOption(key, option.oldEventId, current + surrounding) != option ||
            !ledger.claimLinkRepair(key, approval, reviewed.inventoryVersion, request.revision,
                option.oldEventId, option.replacement?.eventId)
        ) throw StaleTrainingPeaksInventory()

        option.replacement?.let { return TrainingPeaksLinkRepair(it.eventId, recreated = false) }
        if (!baselines.isCurrent(approval)) throw StaleTrainingPeaksInventory()
        return try {
            val eventId = destination.createScopedCalendarEvent(target, account.source, checkNotNull(workout))
            check(ledger.confirmLinkRepair(key, eventId))
            TrainingPeaksLinkRepair(eventId, recreated = true)
        } catch (failure: RuntimeException) {
            log.warn("TrainingPeaks link repair needs review: code=REPAIR_WRITE_UNCERTAIN exception={}", failure.javaClass.simpleName)
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "REPAIR_WRITE_UNCERTAIN")
        }
    }

    private fun repairOption(key: Key, oldEventId: Long, events: List<IntervalsCalendarEventDTO>): MissingLinkRepair? {
        // Look for conflicting ownership or duplicate markers too. Normal matching deliberately filters these out.
        if (events.any { it.id == oldEventId }) return null
        val relevant = events.filter { event ->
            event.description.orEmpty().lineSequence().any { line ->
                line.trim() == "trainingPeaksId=${key.sourceWorkoutId}"
            }
        }
        if (relevant.isEmpty()) return MissingLinkRepair(oldEventId, null)
        val candidate = relevant.map { it.id }.distinct().singleOrNull() ?: return null
        val copies = events.filter { it.id == candidate }
        if (copies.any { it.trainingPeaksId(key.sourceAccountId, allowLegacy = false) != key.sourceWorkoutId } ||
            copies.map { it.start_date_local }.distinct().size != 1
        ) return null
        val verified = previewVerifiedEvent(key, candidate, allowLegacy = false) ?: return null
        if (verified.start_date_local != copies.first().start_date_local) return null
        return MissingLinkRepair(oldEventId, verified.previewCandidate())
    }

    private fun inventory(request: TrainingPeaksPreviewRequest, today: LocalDate): Inventory {
        val (start, end, types) = request
        if (start > end || start < LocalDate.MIN.plusDays(365) || end > LocalDate.MAX.minusDays(365)) {
            throw InvalidTrainingPeaksPreview("INVALID_DATE_RANGE")
        }
        val (generation, inventoryVersion, previous) = previewStage(PreviewStage.BASELINE_READ) {
            Triple(baselines.generation(), baselines.inventoryVersion(), baselines.latest())
        }
        val user = previewStage(PreviewStage.TRAINING_PEAKS_ACCOUNT, PreviewProvider.TRAINING_PEAKS) { source.calendarUser() }
        if (!user.isAthlete) throw InvalidTrainingPeaksPreview("ATHLETE_ACCOUNT_REQUIRED")
        if (!user.isPremium && (start < today || end > today.plusDays(1))) {
            throw InvalidTrainingPeaksPreview("BASIC_TODAY_OR_TOMORROW_ONLY")
        }

        val target = previewStage(PreviewStage.INTERVALS_ACCOUNT, PreviewProvider.INTERVALS) {
            destination.resolveCalendarIdentity()
        }
        val athlete = target.athleteId
        val accounts = Accounts(user.userId, athlete, generation)
        val current = previewStage(PreviewStage.INTERVALS_CALENDAR_RANGE, PreviewProvider.INTERVALS) {
            destination.getCalendarSnapshot(athlete, start, end)
        }
        val surrounding = previewStage(PreviewStage.INTERVALS_CALENDAR_LEGACY_WINDOW, PreviewProvider.INTERVALS) {
            destination.getCalendarSnapshot(athlete, start.minusDays(365), end.plusDays(365))
        }
        val planned = previewStage(PreviewStage.TRAINING_PEAKS_CALENDAR, PreviewProvider.TRAINING_PEAKS) {
            source.getPlannedCalendarEntries(user.userId, start, end)
        }
        val selected = previewStage(PreviewStage.SOURCE_ROWS) {
            planned.filter { workout ->
                val type = workout.getWorkoutType()
                workout.workoutDay.toLocalDate() in start..end && type in types && type != TrainingType.NOTE
            }
        }
        val approval = previous?.takeIf { it.state == "APPROVED" && it.accounts == accounts }
        val selectedById = selected.groupBy { it.id }
        val (rows, additionalRows) = previewStage(PreviewStage.LEDGER_MATCHING) {
            val previousKeys = legacyDestinationIds(user.userId, target)
            val candidates = groupCandidates(user.userId, current, surrounding)
            fun rowFor(id: String, date: LocalDate?): TrainingPeaksSyncPreview {
                val key = Key(user.userId, athlete, id)
                val matches = candidates[id].orEmpty()
                return if (id in previousKeys && ledger.find(key) == null) unverifiedPreviousKey(id, date, matches)
                else previewWorkout(key, date) { matches }.let { row ->
                    if (row.reason == Reason.LINK_MISSING && date != null && approval != null &&
                        !scheduledForwardEnabled && id !in previousKeys && selectedById[id]?.size == 1
                    ) {
                        val option = repairOption(key, row.candidates.single().eventId, current + surrounding)
                        row.copy(missingLinkRepair = option, repairBlockedReason =
                            if (option == null) "REPAIR_REPLACEMENT_UNVERIFIED" else null)
                    } else if (date != null && approval != null && !scheduledForwardEnabled &&
                        row.reason == Reason.PREVIOUS_REVIEW_HOLD && matches.isEmpty() &&
                        selectedById[id]?.size == 1 && ledger.find(key) == null &&
                        baselines.holdReason(key) == Reason.LISTINGS_INCONCLUSIVE.name
                    ) row.copy(canResolveUnmatchedHold = true) else row
                }
            }
            val visible = selected.map { workout ->
                val date = workout.workoutDay.toLocalDate()
                val id = workout.id.takeIf { it.isNotBlank() }
                if (id == null) {
                    TrainingPeaksSyncPreview(null, date, Status.BROKEN_OR_UNVERIFIED_LINK, Reason.SOURCE_ID_MISSING)
                } else rowFor(id, date)
            }
            val visibleSourceIds = visible.mapNotNull { it.sourceWorkoutId }.toSet()
            val additionalIds = (candidates.keys + ledger.unresolvedIds(user.userId, athlete) + previousKeys) - visibleSourceIds
            visible to additionalIds.map { rowFor(it, null) }
        }
        previewStage(PreviewStage.BASELINE_READ) {
            if (generation != baselines.generation() || inventoryVersion != baselines.inventoryVersion() || previous != baselines.latest()) {
                throw StaleTrainingPeaksInventory()
            }
        }

        return previewStage(PreviewStage.REVISION) {
            val stableRows = (rows + additionalRows).map { it.copy(candidates = it.candidates.sortedBy { candidate -> candidate.eventId }) }
                .sortedWith(compareBy({ it.sourceDate }, { it.sourceWorkoutId }, { it.status }, { it.reason }))
            val previousId = previous?.id ?: 0
            val resolvable = stableRows.filter { it.canResolveUnmatchedHold || it.missingLinkRepair != null }.map { selectedById.getValue(it.sourceWorkoutId!!).single() }
            val content = objectMapper.writeValueAsBytes(listOf(
                accounts, previousId, inventoryVersion, previous?.state, start, end, types.distinct().sorted(), stableRows, resolvable,
            ))
            val revision = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content))
            Inventory(accounts, previousId, inventoryVersion, stableRows, revision, approval, selected)
        }
    }

    private fun legacyDestinationIds(source: String, target: CalendarIdentity): Set<String> =
        if (target.configuredAthleteId == target.athleteId) emptySet()
        else ledger.idsForDestination(source, target.configuredAthleteId).toSet()

    private fun unverifiedPreviousKey(id: String, date: LocalDate?, events: List<IntervalsCalendarEventDTO>) =
        TrainingPeaksSyncPreview(
            id, date, Status.BROKEN_OR_UNVERIFIED_LINK, Reason.LEGACY_DESTINATION_KEY_UNVERIFIED,
            events.map { it.previewCandidate() },
        )

    private fun previewWorkout(
        key: Key,
        date: LocalDate?,
        candidates: () -> List<IntervalsCalendarEventDTO>,
    ): TrainingPeaksSyncPreview {
        val entry = ledger.find(key)
        if (entry?.state == State.CONFIRMED) {
            val id = checkNotNull(entry.remoteEventId)
            val event = previewStage(PreviewStage.INTERVALS_EVENT_LOOKUP, PreviewProvider.INTERVALS) {
                destination.getCalendarEvent(key.destinationAthleteId, id)
            }
            val issue = when {
                event == null -> Reason.LINK_MISSING
                event.trainingPeaksId(key.sourceAccountId) != key.sourceWorkoutId -> Reason.LINK_MARKER_CHANGED
                else -> null
            }
            return TrainingPeaksSyncPreview(
                key.sourceWorkoutId,
                date,
                if (issue == null) Status.VERIFIED_LINK else Status.BROKEN_OR_UNVERIFIED_LINK,
                issue ?: Reason.STORED_LINK_VERIFIED,
                listOf(Candidate(id, event?.start_date_local?.toLocalDate())),
            )
        }

        val matches = candidates()
        val single = matches.singleOrNull()
        val candidateDetails = matches.map { Candidate(it.id, it.start_date_local.toLocalDate()) }
        fun result(status: Status, reason: Reason, events: List<Candidate> = candidateDetails) =
            TrainingPeaksSyncPreview(key.sourceWorkoutId, date, status, reason, events)

        if (entry?.state == State.PENDING) {
            val verified = if (ledger.hasPendingRepair(key)) null
            else single?.let { previewVerifiedEvent(key, it.id, allowLegacy = false) }
            val reason = if (verified == null) Reason.CLAIM_PENDING else Reason.CLAIM_CANDIDATE_VERIFIED
            return result(Status.UNRESOLVED_CLAIM, reason, verified?.let { listOf(it.previewCandidate()) } ?: candidateDetails)
        }
        if (matches.size > 1) return result(Status.MULTIPLE_LEGACY_CANDIDATES, Reason.MULTIPLE_MATCHES)
        if (baselines.isHeld(key)) return result(Status.BROKEN_OR_UNVERIFIED_LINK, Reason.PREVIOUS_REVIEW_HOLD)
        if (entry?.state == State.AMBIGUOUS) {
            return result(Status.BROKEN_OR_UNVERIFIED_LINK, Reason.LEGACY_REVIEW_REQUIRED)
        }
        if (single == null) return result(Status.NO_MATCH_OBSERVED, Reason.LISTINGS_INCONCLUSIVE)

        val event = previewVerifiedEvent(key, single.id)
            ?: return result(Status.BROKEN_OR_UNVERIFIED_LINK, Reason.LEGACY_REVIEW_REQUIRED)
        val reason = if (event.trainingPeaksId(key.sourceAccountId, allowLegacy = false) == key.sourceWorkoutId) {
            Reason.CANDIDATE_VERIFIED_BY_ID
        } else {
            Reason.LEGACY_ACCOUNT_UNVERIFIED
        }
        return result(Status.SINGLE_LEGACY_CANDIDATE, reason, listOf(event.previewCandidate()))
    }

    private fun previewVerifiedEvent(key: Key, eventId: Long, allowLegacy: Boolean = true) =
        previewStage(PreviewStage.INTERVALS_EVENT_LOOKUP, PreviewProvider.INTERVALS) {
            verifiedEvent(key, eventId, allowLegacy)
        }

    private fun IntervalsCalendarEventDTO.previewCandidate() = Candidate(id, start_date_local.toLocalDate())

    private fun findCandidates(
        sourceAccount: String, athlete: String, start: LocalDate, end: LocalDate,
    ): Map<String, List<IntervalsCalendarEventDTO>> {
        val current = destination.getCalendarSnapshot(athlete, start, end)
        val surrounding = destination.getCalendarSnapshot(athlete, start.minusDays(365), end.plusDays(365))
        return groupCandidates(sourceAccount, current, surrounding)
    }

    private fun groupCandidates(sourceAccount: String, current: List<IntervalsCalendarEventDTO>, surrounding: List<IntervalsCalendarEventDTO>) =
        (current + surrounding).distinctBy { it.id }
            .mapNotNull { event -> event.trainingPeaksId(sourceAccount)?.let { it to event } }
            .groupBy({ it.first }, { it.second })

    private fun verifiedEvent(key: Key, eventId: Long, allowLegacy: Boolean = true): IntervalsCalendarEventDTO? =
        destination.getCalendarEvent(key.destinationAthleteId, eventId)
            ?.takeIf { it.trainingPeaksId(key.sourceAccountId, allowLegacy) == key.sourceWorkoutId }

    private fun reviewAfterConflict(key: Key): WorkoutSyncReview? =
        reviewKnown(key, checkNotNull(ledger.find(key))) { emptyList() }

    private fun reviewKnown(key: Key, entry: TrainingPeaksSyncLedger.Entry, candidates: () -> List<Long>): WorkoutSyncReview? {
        return when (entry.state) {
            State.AMBIGUOUS -> review(key, WorkoutSyncReview.Reason.AMBIGUOUS_IMPORT)
            State.CONFIRMED -> {
                val eventId = checkNotNull(entry.remoteEventId)
                val event = destination.getCalendarEvent(key.destinationAthleteId, eventId)
                when {
                    event == null -> review(key, WorkoutSyncReview.Reason.LINKED_EVENT_MISSING, eventId)
                    event.trainingPeaksId(key.sourceAccountId) != key.sourceWorkoutId ->
                        review(key, WorkoutSyncReview.Reason.LINKED_EVENT_CHANGED, eventId)
                    else -> null
                }
            }
            State.PENDING -> {
                if (ledger.hasPendingRepair(key)) return review(key, WorkoutSyncReview.Reason.UNRESOLVED_WRITE)
                val matches = candidates()
                val candidate = matches.singleOrNull()
                if (candidate != null &&
                    verifiedEvent(key, candidate, allowLegacy = false) != null
                ) {
                    ledger.confirm(key, candidate)
                    null
                } else {
                    val reason = if (matches.size > 1) WorkoutSyncReview.Reason.AMBIGUOUS_IMPORT
                    else WorkoutSyncReview.Reason.UNRESOLVED_WRITE
                    review(key, reason)
                }
            }
        }
    }

    private fun review(key: Key, reason: WorkoutSyncReview.Reason, eventId: Long? = null): WorkoutSyncReview {
        log.warn("TrainingPeaks sync needs review: code={}", reason)
        return WorkoutSyncReview(key.sourceWorkoutId, reason, eventId)
    }

    private fun IntervalsCalendarEventDTO.trainingPeaksId(sourceAccountId: String, allowLegacy: Boolean = true): String? {
        if (category != "WORKOUT" || type == "NOTE") return null
        val markers = markers()
        val accounts = markers["trainingPeaksAccountId"].orEmpty()
        if (accounts.isEmpty()) {
            if (!allowLegacy) return null
        } else if (accounts.singleOrNull() != sourceAccountId) {
            return null
        }
        return markers["trainingPeaksId"]?.singleOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun IntervalsCalendarEventDTO.markers(): Map<String, List<String>> =
        description.orEmpty().substringAfterLast("//////////", missingDelimiterValue = "")
            .lineSequence().map { it.trim().split('=', limit = 2) }
            .filter { it.size == 2 }.groupBy({ it[0] }, { it[1] })

}
