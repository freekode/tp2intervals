package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

abstract class TPBaseWorkoutResponseDTO(
    val id: String,
    val workoutTypeValueId: Int?,
    val title: String?,
    val totalTimePlanned: Double?,
    val tssPlanned: Int?,
    val description: String?,
    val coachComments: String?,
    val structure: TPWorkoutStructureDTO?
) {
    fun getWorkoutType(): TrainingType? = workoutTypeValueId?.let { TPTrainingTypeMapper.getByValue(it) }
}
