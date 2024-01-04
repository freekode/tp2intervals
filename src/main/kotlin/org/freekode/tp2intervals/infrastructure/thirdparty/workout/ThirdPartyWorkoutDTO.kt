package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure.ThirdPartyWorkoutStructureDTO
import java.time.LocalDateTime

class ThirdPartyWorkoutDTO(
    var workoutId: String,
    var workoutDay: LocalDateTime,
    var workoutTypeValueId: Int?,
    var title: String,
    var totalTimePlanned: Double?,
    var tssPlanned: Double?,
    var description: String?,
    var coachComments: String?,
    var structure: ThirdPartyWorkoutStructureDTO?
) {
    fun getWorkoutType() = workoutTypeValueId?.let { ThirdPartyWorkoutType.findByValue(it) }
}
