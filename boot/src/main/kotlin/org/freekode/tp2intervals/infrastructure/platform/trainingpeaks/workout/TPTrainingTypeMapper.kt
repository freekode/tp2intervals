package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.TrainingType

class TPTrainingTypeMapper {
    companion object {
        private val typeMap = mapOf(
            TrainingType.SWIM to 1,
            TrainingType.BIKE to 2,
            TrainingType.VIRTUAL_BIKE to 2,
            TrainingType.RUN to 3,
            TrainingType.MTB to 8,
            TrainingType.WEIGHT to 9,
            TrainingType.NOTE to 7, // day off
            TrainingType.UNKNOWN to 4, // brick
            TrainingType.UNKNOWN to 5, // crosstrain
            TrainingType.UNKNOWN to 9, // custom
            TrainingType.UNKNOWN to 11, // xc-ski
            TrainingType.UNKNOWN to 12, // rowing
            TrainingType.UNKNOWN to 13, // walk
            TrainingType.UNKNOWN to 100 // other
        )

        fun getByValue(value: Int): TrainingType =
            typeMap.filterValues { it == value }.keys.firstOrNull() ?: TrainingType.UNKNOWN

        fun getByType(trainingType: TrainingType): Int = typeMap[trainingType]!!

    }
}
