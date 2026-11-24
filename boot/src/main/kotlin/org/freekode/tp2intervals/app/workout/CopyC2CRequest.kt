package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToCalendarRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val defaultStartTime: String? = null,
    val refreshSynced: Boolean = false,
    val removeStaleTrainerRoad: Boolean = false,
    val trainerRoadSyncMode: TrainerRoadSyncMode? = null,
    val allowStaleRemoval: Boolean = false,
    val expectedActions: List<SyncPlanAction>? = null,
    val expectedUnidentifiedActivityDates: List<LocalDate>? = null,
)

enum class TrainerRoadSyncMode {
    MIRROR, ADD_MISSING
}

data class SyncPlanAction(
    val action: SyncAction,
    val date: LocalDate,
    val name: String,
    val trainerRoadId: String,
    val intervalsId: String?,
    val trainingPeaksId: String? = null,
)

enum class SyncAction {
    ADD, UPDATE, REMOVE
}