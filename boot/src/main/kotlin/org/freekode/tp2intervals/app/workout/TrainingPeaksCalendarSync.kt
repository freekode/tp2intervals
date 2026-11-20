package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Candidate
import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Reason
import org.freekode.tp2intervals.app.workout.TrainingPeaksSyncPreview.Status
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDate

@Service
class TrainingPeaksCalendarSync(
    private val source: TrainingPeaksWorkoutRepository,
    private val destination: IntervalsWorkoutRepository,
    private val ledger: TrainingPeaksSyncLedger,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun copy(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        check(!TransactionSynchronizationManager.isActualTransactionActive()) {
            "TrainingPeaks sync must commit each claim before calling Intervals.icu"
        }

        val sourceAccount = source.calendarAccountId()
        val athlete = destination.calendarAthleteId()
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
                if (workout !in plannedWorkouts) {
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
                ledger.claim(key) -> {
                    try {
                        val eventId = destination.createCalendarEvent(athlete, sourceAccount, workout)
                        ledger.confirm(key, eventId)
                        copied++
                    } catch (failure: RuntimeException) {
                        review(key, WorkoutSyncReview.Reason.UNRESOLVED_WRITE)
                        throw failure
                    }
                }
                else -> reviewAfterConflict(key)?.let(reviews::add)
            }
        }

        return CopyWorkoutsResponse(copied, workouts.size - copied, request.startDate, request.endDate, ExternalData.empty(), reviews)
    }

    fun preview(request: TrainingPeaksPreviewRequest, today: LocalDate = LocalDate.now()): List<TrainingPeaksSyncPreview> {
        val (start, end, types) = request
        if (start > end || start < LocalDate.MIN.plusDays(365) || end > LocalDate.MAX.minusDays(365)) {
            throw InvalidTrainingPeaksPreview("INVALID_DATE_RANGE")
        }
        val user = source.calendarUser()
        if (!user.isAthlete) throw InvalidTrainingPeaksPreview("ATHLETE_ACCOUNT_REQUIRED")
        if (!user.isPremium && (start < today || end > today.plusDays(1))) {
            throw InvalidTrainingPeaksPreview("BASIC_TODAY_OR_TOMORROW_ONLY")
        }

        val athlete = destination.calendarAthleteId()
        val candidates by lazy { findCandidates(user.userId, athlete, start, end) }
        return source.getPlannedCalendarEntries(user.userId, start, end)
            .filter { workout ->
                val type = workout.getWorkoutType()
                workout.workoutDay.toLocalDate() in start..end && type in types && type != TrainingType.NOTE
            }
            .map { workout ->
                val date = workout.workoutDay.toLocalDate()
                val id = workout.id.takeIf { it.isNotBlank() }
                if (id == null) {
                    TrainingPeaksSyncPreview(null, date, Status.BROKEN_OR_UNVERIFIED_LINK, Reason.SOURCE_ID_MISSING)
                } else {
                    val key = Key(user.userId, athlete, id)
                    previewWorkout(key, date) { candidates[id].orEmpty() }
                }
            }
    }

    private fun previewWorkout(
        key: Key,
        date: LocalDate,
        candidates: () -> List<IntervalsEventDTO>,
    ): TrainingPeaksSyncPreview {
        val entry = ledger.find(key)
        if (entry?.state == State.CONFIRMED) {
            val id = checkNotNull(entry.remoteEventId)
            val event = destination.getCalendarEvent(key.destinationAthleteId, id)
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
            val verified = single?.let { verifiedEvent(key, it.id, allowLegacy = false) }
            val reason = if (verified == null) Reason.CLAIM_PENDING else Reason.CLAIM_CANDIDATE_VERIFIED
            return result(Status.UNRESOLVED_CLAIM, reason, verified?.let { listOf(it.previewCandidate()) } ?: candidateDetails)
        }
        if (matches.size > 1) return result(Status.MULTIPLE_LEGACY_CANDIDATES, Reason.MULTIPLE_MATCHES)
        if (entry?.state == State.AMBIGUOUS) {
            return result(Status.BROKEN_OR_UNVERIFIED_LINK, Reason.LEGACY_REVIEW_REQUIRED)
        }
        if (single == null) return result(Status.NO_MATCH_OBSERVED, Reason.LISTINGS_INCONCLUSIVE)

        val event = verifiedEvent(key, single.id)
            ?: return result(Status.BROKEN_OR_UNVERIFIED_LINK, Reason.LEGACY_REVIEW_REQUIRED)
        val reason = if (event.trainingPeaksId(key.sourceAccountId, allowLegacy = false) == key.sourceWorkoutId) {
            Reason.CANDIDATE_VERIFIED_BY_ID
        } else {
            Reason.LEGACY_ACCOUNT_UNVERIFIED
        }
        return result(Status.SINGLE_LEGACY_CANDIDATE, reason, listOf(event.previewCandidate()))
    }

    private fun IntervalsEventDTO.previewCandidate() = Candidate(id, start_date_local.toLocalDate())

    private fun findCandidates(
        sourceAccount: String, athlete: String, start: LocalDate, end: LocalDate,
    ): Map<String, List<IntervalsEventDTO>> {
        val current = destination.getCalendarSnapshot(athlete, start, end)
        val surrounding = destination.getCalendarSnapshot(athlete, start.minusDays(365), end.plusDays(365))
        return (current + surrounding).distinctBy { it.id }
            .mapNotNull { event -> event.trainingPeaksId(sourceAccount)?.let { it to event } }
            .groupBy({ it.first }, { it.second })
    }

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
