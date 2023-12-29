package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.TrainingType

enum class ThirdPartyWorkoutType(
    val value: Int,
    val trainingType: TrainingType
) {
    BIKE(2, TrainingType.BIKE),
    RUN(3, TrainingType.RUN),
    NOTE(7, TrainingType.NOTE),
    MTB(8, TrainingType.BIKE),
    WEIGHT(9, TrainingType.WEIGHT),
    WALKING(13, TrainingType.WALK);

    companion object {
        fun findByValue(value: Int): ThirdPartyWorkoutType {
            return entries.find { it.value == value } ?: throw RuntimeException("$value is unknown workout type")
        }

        fun findByType(trainingType: TrainingType): ThirdPartyWorkoutType {
            return entries.find { it.trainingType == trainingType }!!
        }

    }
}
