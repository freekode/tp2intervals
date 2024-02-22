package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.PlatformException

class TPTargetMapper {
    companion object {
        private val targetMap = mapOf(
            WorkoutStructure.TargetUnit.FTP_PERCENTAGE to "percentOfFtp",
            WorkoutStructure.TargetUnit.LTHR_PERCENTAGE to "percentOfThresholdHr",
            WorkoutStructure.TargetUnit.PACE_PERCENTAGE to "percentOfThresholdPace",
        )

        fun getByIntensity(intensity: String): WorkoutStructure.TargetUnit =
            targetMap.filterValues { it == intensity }.keys.firstOrNull()
                ?: throw PlatformException("Cant convert intensity $intensity")

        fun getByTargetUnit(targetUnit: WorkoutStructure.TargetUnit): String =
            targetMap[targetUnit] ?: throw PlatformException("Cant convert target $targetUnit")
    }
}
