package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Candidate
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
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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

    fun copyScheduled(request: CopyFromCalendarToCalendarRequest, today: LocalDate = LocalDate.now()) {
        if (!scheduledForwardEnabled || mode.enabled || !request.skipSynced ||
            request.sourcePlatform != Platform.TRAINING_PEAKS ||
            request.targetPlatform != Platform.INTERVALS
        ) return

        // An unapproved installation has no reason to contact the providers from a job.
        if (baselines.latest()?.state != "APPROVED") {
            log.info("Scheduled TrainingPeaks sync paused: BASELINE_REQUIRED")
            return
        }
        val generation = baselines.generation()
        val user = source.calendarUser()
        val target = destination.resolveCalendarIdentity()
        val accounts = Accounts(user.userId, target.athleteId, generation)
        val approval = baselines.usableApproval(accounts) ?: return
        if (!user.isAthlete || request.startDate > request.endDate ||
            (!user.isPremium && (request.startDate != today || request.endDate != today))
        ) return

        copy(request, user.userId, target, approval)
    }

    fun copy(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        mode.requireWritable()
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
        val workouts = source.getWorkoutsFromCalendar(sourceAccount, request.startDate, request.endDate)
        val reviews = mutableListOf<WorkoutSyncReview>()
        val legacyMatches by lazy {
            findCandidates(sourceAccount, athlete, request.startDate, request.endDate)
        }
        val plannedWorkouts by lazy { destination.getWorkoutsFromCalendar(request.startDate, request.endDate) }

        var copied = 0
        for (workout in workouts.filter { it.details.type in request.types }) {
            val sourceId = workout.details.externalData.trainingPeaksId
            if (sourceId.isNullOrBlank() || workout.details.type == TrainingType.NOTE) {
                if (approval == null && workout !in plannedWorkouts) {
                    destination.saveWorkoutsToCalendar(listOf(workout))
                    copied++
                }
                continue
            }

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

        return CopyWorkoutsResponse(copied, workouts.size - copied, request.startDate, request.endDate, ExternalData.empty(), reviews)
    }

    private data class Inventory(
        val accounts: Accounts,
        val previousId: Long,
        val inventoryVersion: Long,
        val rows: List<TrainingPeaksSyncPreview>,
        val revision: String,
        val approval: Approval?,
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
        val (rows, additionalRows) = previewStage(PreviewStage.LEDGER_MATCHING) {
            val previousKeys = legacyDestinationIds(user.userId, target)
            val candidates = groupCandidates(user.userId, current, surrounding)
            fun rowFor(id: String, date: LocalDate?): TrainingPeaksSyncPreview {
                val key = Key(user.userId, athlete, id)
                val matches = candidates[id].orEmpty()
                return if (id in previousKeys && ledger.find(key) == null) unverifiedPreviousKey(id, date, matches)
                else previewWorkout(key, date) { matches }
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
            val content = objectMapper.writeValueAsBytes(listOf(
                accounts, previousId, inventoryVersion, previous?.state, start, end, types.distinct().sorted(), stableRows,
            ))
            val revision = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content))
            Inventory(accounts, previousId, inventoryVersion, stableRows, revision, previous?.takeIf {
                it.state == "APPROVED" && it.accounts == accounts
            })
        }
    }

    private fun legacyDestinationIds(source: String, target: CalendarIdentity): Set<String> =
        if (target.configuredAthleteId == target.athleteId) emptySet()
        else ledger.idsForDestination(source, target.configuredAthleteId).toSet()

    private fun unverifiedPreviousKey(id: String, date: LocalDate?, events: List<IntervalsEventDTO>) =
        TrainingPeaksSyncPreview(
            id, date, Status.BROKEN_OR_UNVERIFIED_LINK, Reason.LEGACY_DESTINATION_KEY_UNVERIFIED,
            events.map { it.previewCandidate() },
        )

    private fun previewWorkout(
        key: Key,
        date: LocalDate?,
        candidates: () -> List<IntervalsEventDTO>,
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
            val verified = single?.let { previewVerifiedEvent(key, it.id, allowLegacy = false) }
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

    private fun IntervalsEventDTO.previewCandidate() = Candidate(id, start_date_local.toLocalDate())

    private fun findCandidates(
        sourceAccount: String, athlete: String, start: LocalDate, end: LocalDate,
    ): Map<String, List<IntervalsEventDTO>> {
        val current = destination.getCalendarSnapshot(athlete, start, end)
        val surrounding = destination.getCalendarSnapshot(athlete, start.minusDays(365), end.plusDays(365))
        return groupCandidates(sourceAccount, current, surrounding)
    }

    private fun groupCandidates(sourceAccount: String, current: List<IntervalsEventDTO>, surrounding: List<IntervalsEventDTO>) =
        (current + surrounding).distinctBy { it.id }
            .mapNotNull { event -> event.trainingPeaksId(sourceAccount)?.let { it to event } }
            .groupBy({ it.first }, { it.second })

    private fun verifiedEvent(key: Key, eventId: Long, allowLegacy: Boolean = true): IntervalsEventDTO? =
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
        log.warn("TrainingPeaks sync needs review: {} for workout {}, Intervals athlete {}, event {}",
            reason, key.sourceWorkoutId, key.destinationAthleteId, eventId)
        return WorkoutSyncReview(key.sourceWorkoutId, reason, eventId)
    }

    private fun IntervalsEventDTO.trainingPeaksId(sourceAccountId: String, allowLegacy: Boolean = true): String? {
        if (!isWorkout() || type == "NOTE") return null
        val footer = description.orEmpty().substringAfterLast("//////////", missingDelimiterValue = "")
        val markers = footer.lineSequence().map { it.trim().split('=', limit = 2) }
            .filter { it.size == 2 }.groupBy({ it[0] }, { it[1] })
        val accounts = markers["trainingPeaksAccountId"].orEmpty()
        if (accounts.isEmpty()) {
            if (!allowLegacy) return null
        } else if (accounts.singleOrNull() != sourceAccountId) {
            return null
        }
        return markers["trainingPeaksId"]?.singleOrNull()?.takeIf { it.isNotBlank() }
    }
}
