package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsException

class IntervalsWorkoutTargetMapper(
    private val zones: List<IntervalsWorkoutDocDTO.IntervalsWorkoutZoneDTO>?
) {
    fun mapMainTarget(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutStepTarget {
        val mainTarget = if (stepDTO.power != null) {
            toWorkoutStepTarget(stepDTO.power)
        } else if (stepDTO.hr != null) {
            toWorkoutStepTarget(stepDTO.hr)
        } else if (stepDTO.pace != null) {
            throw IntervalsException("Can't handle steps with pace target yet")
        } else {
            throw IntervalsException("Unknown target in step $stepDTO")
        }
        return mainTarget
    }

    fun mapCadenceTarget(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO?): WorkoutStepTarget? {
        return stepValueDTO?.let { mapSimpleUnit(WorkoutStepTarget.TargetUnit.RPM, it) }
    }

    private fun toWorkoutStepTarget(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): WorkoutStepTarget {
        return when (stepValueDTO.units) {
            "%ftp" -> mapSimpleUnit(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, stepValueDTO)
            "rpm" -> mapSimpleUnit(WorkoutStepTarget.TargetUnit.RPM, stepValueDTO)
            "power_zone" -> mapPowerZoneTarget(stepValueDTO)
            else -> throw IntervalsException("Can't handle ${stepValueDTO.units} units ")
        }
    }

    private fun mapSimpleUnit(
        targetUnit: WorkoutStepTarget.TargetUnit,
        stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO
    ): WorkoutStepTarget {
        val (min, max) = mapTargetValuePair(stepValueDTO)
        return WorkoutStepTarget(targetUnit, min, max)
    }

    private fun mapPowerZoneTarget(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): WorkoutStepTarget {
        val zoneMap = getZoneMap()

        val targetRange = if (stepValueDTO.value != null) {
            zoneMap[stepValueDTO.value]!!.getRange()
        } else {
            listOf(
                zoneMap[stepValueDTO.start]!!.getRange().min(),
                zoneMap[stepValueDTO.end]!!.getRange().max()
            )
        }

        return WorkoutStepTarget(
            WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE,
            targetRange[0],
            targetRange[1]
        )
    }

    private fun mapTargetValuePair(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): Pair<Int, Int> =
        if (stepValueDTO.value != null) {
            stepValueDTO.value to stepValueDTO.value
        } else {
            stepValueDTO.start!! to stepValueDTO.end!!
        }

    private fun getZoneMap(): Map<Int, IntervalsWorkoutDocDTO.IntervalsWorkoutZoneDTO> =
        zones!!.associateBy { it.zone!! }
}
