package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.TrainingType

class IntervalsEventTypeMapper {
    companion object {
        private val typeMap = mapOf(
            TrainingType.BIKE to "Ride",
            TrainingType.MTB to "MountainBikeRide",
            TrainingType.VIRTUAL_BIKE to "VirtualRide",
//            TrainingType.RUN to "Run",
            TrainingType.WEIGHT to "WeightTraining",
            TrainingType.WALK to "Walk",
            TrainingType.NOTE to "NOTE",
            TrainingType.UNKNOWN to "Other",
        )

        fun getByIntervalsType(intervalsType: String): TrainingType =
            typeMap.filterValues { it == intervalsType }.keys.firstOrNull() ?: TrainingType.UNKNOWN

        fun getByTrainingType(trainingType: TrainingType): String = typeMap[trainingType]!!
    }
}
