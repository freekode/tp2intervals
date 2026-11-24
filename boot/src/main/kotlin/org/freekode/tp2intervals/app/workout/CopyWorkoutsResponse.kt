package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import java.time.LocalDate

enum class SkippedSyncReason {
    DUPLICATE_TRAINERROAD_ID,
    DUPLICATE_INTERVALS_ID,
    PROTECTED_EVENT,
    UNIDENTIFIED_ACTIVITY,
}

data class SkippedSyncWorkout(
    val date: LocalDate,
    val name: String,
    val trainerRoadId: String,
    val intervalsIds: List<String>,
    val reason: SkippedSyncReason,
)

data class CopyWorkoutsResponse(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val externalData: ExternalData,
    val alreadySynced: Int = 0,
    val removed: Int = 0,
    val skipped: Int = 0,
    val updated: Int = 0,
    val previewActions: List<SyncPlanAction> = emptyList(),
    val unidentifiedActivityDates: List<LocalDate> = emptyList(),
    val skippedWorkouts: List<SkippedSyncWorkout> = emptyList(),
)
