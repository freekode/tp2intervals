package org.freekode.tp2intervals.app.workout

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
            // Look beyond the original dates for legacy imports that an athlete moved.
            val current = destination.getCalendarSnapshot(athlete, request.startDate, request.endDate)
            val surrounding = destination.getCalendarSnapshot(
                athlete, request.startDate.minusDays(365), request.endDate.plusDays(365)
            )
            (current + surrounding).distinctBy { it.id }
                .mapNotNull { event -> event.trainingPeaksId(sourceAccount)?.let { it to event.id } }
                .groupBy({ it.first }, { it.second })
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
                reviewKnown(key, existing, { legacyMatches[sourceId].orEmpty() })?.let(reviews::add)
                continue
            }

            val candidates = legacyMatches[sourceId].orEmpty()
            when {
                candidates.size > 1 -> {
                    ledger.recordAmbiguousMatch(key)
                    reviews.add(review(key, WorkoutSyncReview.Reason.AMBIGUOUS_IMPORT))
                }
                candidates.size == 1 -> {
                    val eventId = candidates.single()
                    if (destination.getCalendarEvent(athlete, eventId)?.trainingPeaksId(sourceAccount) == sourceId) {
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
                    destination.getCalendarEvent(key.destinationAthleteId, candidate)?.trainingPeaksId(key.sourceAccountId, allowLegacy = false) == key.sourceWorkoutId
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
