package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPBaseWorkoutResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

class TPWorkoutLibraryItemDTO(
    exerciseLibraryItemId: String,
    workoutTypeId: Int,
    itemName: String,
    totalTimePlanned: Double?,
    tssPlanned: Int?,
    description: String?,
    coachComments: String?,
    structure: TPWorkoutStructureDTO?
) : TPBaseWorkoutResponseDTO(
    exerciseLibraryItemId,
    workoutTypeId,
    itemName,
    totalTimePlanned,
    tssPlanned,
    description,
    coachComments,
    structure
)
