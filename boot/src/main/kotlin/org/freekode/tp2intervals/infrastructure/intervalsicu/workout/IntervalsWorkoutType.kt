package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.TrainingType

enum class IntervalsWorkoutType(
    val trainingType: TrainingType
) {
    Ride(TrainingType.BIKE),
    VirtualRide(TrainingType.BIKE),
    Run(TrainingType.RUN),
    WeightTraining(TrainingType.WEIGHT),
    NOTE(TrainingType.NOTE),
    Walk(TrainingType.WALK),
    Other(TrainingType.UNKNOWN);

    companion object {
        fun findByType(trainingType: TrainingType): IntervalsWorkoutType {
            return entries.find { it.trainingType == trainingType }!!
        }
    }
}
