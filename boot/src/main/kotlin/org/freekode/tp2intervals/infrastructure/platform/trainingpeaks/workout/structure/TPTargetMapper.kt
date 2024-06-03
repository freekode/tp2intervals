package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepStructure

class TPTargetMapper {
    companion object {
        private val targetMap = mapOf(
            StepStructure.TargetUnit.FTP_PERCENTAGE to "percentOfFtp",
            StepStructure.TargetUnit.LTHR_PERCENTAGE to "percentOfThresholdHr",
            StepStructure.TargetUnit.PACE_PERCENTAGE to "percentOfThresholdPace",
        )

        fun getByIntensity(intensity: String): StepStructure.TargetUnit =
            targetMap.filterValues { it == intensity }.keys.firstOrNull()
                ?: throw IllegalArgumentException("Cant convert intensity $intensity")

        fun getByTargetUnit(targetUnit: StepStructure.TargetUnit): String =
            targetMap[targetUnit] ?: throw IllegalArgumentException("Cant convert target $targetUnit")
    }
}
