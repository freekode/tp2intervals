package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.domain.workout.WorkoutType

enum class IntervalsWorkoutType(
    val workoutType: WorkoutType
) {
    Ride(WorkoutType.BIKE),
    Run(WorkoutType.RUN),
    WeightTraining(WorkoutType.WEIGHT),
    NOTE(WorkoutType.NOTE);

    companion object {
        fun findByType(workoutType: WorkoutType): IntervalsWorkoutType {
            return entries.find { it.workoutType == workoutType }!!
        }
    }
}
