package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.TrainingType

class TPTrainingTypeMapper {
    companion object {
        private val typeMap = mapOf(
            //WORKOUT
            TrainingType.SWIM to 1,
            TrainingType.BIKE to 2,
            TrainingType.VIRTUAL_BIKE to 2,
            TrainingType.RUN to 3,
            TrainingType.BRICK to 4,
            TrainingType.UNKNOWN to 5, // crosstrain
            TrainingType.RACE to 6,
            TrainingType.DAY_OFF to 7,
            TrainingType.NOTE to 7,
            TrainingType.MTB to 8,
            TrainingType.STRENGTH to 9,
            TrainingType.UNKNOWN to 9, // custom
            TrainingType.UNKNOWN to 11, // xc-ski
            TrainingType.UNKNOWN to 12, // rowing
            TrainingType.UNKNOWN to 13, // walk
            TrainingType.UNKNOWN to 100, // other

            //WELLNESS (METRICS)
            TrainingType.WEIGHT to 9
        )

        fun getByValue(value: Int): TrainingType =
            typeMap.filterValues { it == value }.keys.firstOrNull() ?: TrainingType.UNKNOWN

        fun getByType(trainingType: TrainingType): Int = typeMap[trainingType]!!

    }
}
