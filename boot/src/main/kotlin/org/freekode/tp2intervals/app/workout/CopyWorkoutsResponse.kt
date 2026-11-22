package org.freekode.tp2intervals.app.workout

import com.fasterxml.jackson.annotation.JsonInclude
import org.freekode.tp2intervals.domain.ExternalData
import java.time.LocalDate

data class CopyWorkoutsResponse(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val externalData: ExternalData,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val syncReview: List<WorkoutSyncReview> = emptyList(),
)

data class WorkoutSyncReview(val sourceWorkoutId: String, val reason: Reason, val remoteEventId: Long?) {
    enum class Reason {
        UNRESOLVED_WRITE,
        LEGACY_BASELINE_HOLD,
        AMBIGUOUS_IMPORT,
        LEGACY_LINK_UNVERIFIED,
        LINKED_EVENT_MISSING,
        LINKED_EVENT_CHANGED,
    }
}


data class ScheduledCopyOutcome(
    val result: CopyWorkoutsResponse? = null,
    val blockedReason: String? = null,
)
