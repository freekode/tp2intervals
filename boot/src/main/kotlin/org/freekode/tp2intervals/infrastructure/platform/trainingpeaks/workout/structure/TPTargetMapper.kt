package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class TPTargetMapper {
    companion object {
        private val targetMap = mapOf(
            WorkoutStructure.TargetUnit.FTP_PERCENTAGE to "percentOfFtp",
            WorkoutStructure.TargetUnit.LTHR_PERCENTAGE to "percentOfThresholdHr",
            WorkoutStructure.TargetUnit.PACE_PERCENTAGE to "percentOfThresholdPace",
            WorkoutStructure.TargetUnit.RELATIVE_PERCEIVED_EFFORT to "rpe",
        )

        fun getByIntensity(intensity: String): WorkoutStructure.TargetUnit =
            targetMap.filterValues { it == intensity }.keys.firstOrNull()
                ?: throw IllegalArgumentException("Cant convert intensity $intensity")

        fun getByTargetUnit(targetUnit: WorkoutStructure.TargetUnit): String =
            targetMap[targetUnit] ?: throw IllegalArgumentException("Cant convert target $targetUnit")
    }
}
