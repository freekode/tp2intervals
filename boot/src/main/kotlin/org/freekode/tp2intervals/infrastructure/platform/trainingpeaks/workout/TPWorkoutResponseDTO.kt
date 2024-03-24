package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO
import java.time.LocalDateTime
import org.freekode.tp2intervals.domain.TrainingType

class TPWorkoutResponseDTO(
    val workoutDay: LocalDateTime,
    workoutId: String,
    workoutTypeValueId: Int?,
    title: String,
    totalTimePlanned: Double?,
    tssPlanned: Int?,
    description: String?,
    coachComments: String?,
    structure: TPWorkoutStructureDTO?
): TPBaseWorkoutResponseDTO(
    workoutId,
    workoutTypeValueId,
    title,
    totalTimePlanned,
    tssPlanned,
    description,
    coachComments,
    structure
)
