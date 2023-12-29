package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.workout.WorkoutType

enum class ThirdPartyWorkoutType(
    val value: Int,
    val workoutType: WorkoutType
) {
    BIKE(2, WorkoutType.BIKE),
    RUN(3, WorkoutType.RUN),
    NOTE(7, WorkoutType.NOTE),
    MTB(8, WorkoutType.BIKE),
    WEIGHT(9, WorkoutType.WEIGHT),
    WALKING(13, WorkoutType.WALK);

    companion object {
        fun findByValue(value: Int): ThirdPartyWorkoutType {
            return entries.find { it.value == value } ?: throw RuntimeException("$value is unknown workout type")
        }

        fun findByType(workoutType: WorkoutType): ThirdPartyWorkoutType {
            return entries.find { it.workoutType == workoutType }!!
        }

    }
}
