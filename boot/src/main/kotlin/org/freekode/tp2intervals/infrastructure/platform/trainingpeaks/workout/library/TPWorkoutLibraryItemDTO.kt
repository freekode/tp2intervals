package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.library

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPBaseWorkoutResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

class TPWorkoutLibraryItemDTO(
    exerciseLibraryItemId: String,
    workoutTypeValueId: Int,
    itemName: String,
    totalTimePlanned: Double?,
    tssPlanned: Int?,
    description: String?,
    coachComments: String?,
    structure: TPWorkoutStructureDTO?
) : TPBaseWorkoutResponseDTO(
    exerciseLibraryItemId,
    workoutTypeValueId,
    itemName,
    totalTimePlanned,
    tssPlanned,
    description,
    coachComments,
    structure
)
