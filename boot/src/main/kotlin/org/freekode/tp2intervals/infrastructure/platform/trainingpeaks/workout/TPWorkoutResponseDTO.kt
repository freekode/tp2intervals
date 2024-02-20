package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.LocalDateTime
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

class TPWorkoutResponseDTO(
    var workoutId: String,
    var workoutDay: LocalDateTime,
    var workoutTypeValueId: Int?,
    var title: String,
    var totalTimePlanned: Double?,
    var tssPlanned: Double?,
    var description: String?,
    var coachComments: String?,
    var structure: TPWorkoutStructureDTO?
) {
    fun getWorkoutType(): TrainingType? = workoutTypeValueId?.let { TPWorkoutTypeMapper.getByValue(it) }
}
