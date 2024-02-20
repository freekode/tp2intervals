package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.TrainingType

class TPWorkoutTypeMapper {
    companion object {
        private val typeMap = mapOf(
            TrainingType.BIKE to 2,
            TrainingType.MTB to 8,
            TrainingType.VIRTUAL_BIKE to 2,
//            TrainingType.RUN to 3,
            TrainingType.NOTE to 7,
            TrainingType.WEIGHT to 9,
            TrainingType.WALK to 13,
            TrainingType.UNKNOWN to 100
        )

        fun getByValue(value: Int): TrainingType =
            typeMap.filterValues { it == value }.keys.firstOrNull() ?: TrainingType.UNKNOWN

        fun getByType(trainingType: TrainingType): Int = typeMap[trainingType]!!

    }
}
