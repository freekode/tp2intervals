package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class IntervalsWorkoutTargetMapper(
    zones: List<IntervalsWorkoutZoneDTO>
) {
    private val zoneMap: Map<Int, IntervalsWorkoutZoneDTO> = zones.associateBy { it.zone }

    fun map(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): WorkoutStepTarget {
        return when (stepValueDTO.units) {
            "%ftp" -> mapSimpleUnit(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, stepValueDTO)
            "%lthr" -> mapSimpleUnit(WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE, stepValueDTO)
            "rpm" -> mapSimpleUnit(WorkoutStepTarget.TargetUnit.RPM, stepValueDTO)
            "power_zone" -> mapPowerZoneTarget(stepValueDTO)
            else -> throw RuntimeException("Can't handle units for ${stepValueDTO.units}")
        }
    }

    private fun mapSimpleUnit(
        targetUnit: WorkoutStepTarget.TargetUnit,
        stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO
    ): WorkoutStepTarget {
        val (min, max) = mapTargetValue(stepValueDTO)
        return WorkoutStepTarget(targetUnit, min, max)
    }

    private fun mapPowerZoneTarget(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): WorkoutStepTarget {
        val zonePercentageRange = zoneMap[stepValueDTO.value]!!.getPercentagePair()
        return WorkoutStepTarget(
            WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE,
            zonePercentageRange.first,
            zonePercentageRange.second
        )
    }

    private fun mapTargetValue(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): Pair<Int, Int> =
        if (stepValueDTO.value != null) {
            stepValueDTO.value to stepValueDTO.value
        } else {
            stepValueDTO.start!! to stepValueDTO.end!!
        }

}
