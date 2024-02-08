package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsException

class IntervalsWorkoutTargetMapper(
    private val ftp: Double?,
    private val lthr: Double?,
    private val paceThreshold: Double?
) {
    fun mapMainTarget(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutStepTarget {
        return if (stepDTO._power != null) {
            toWorkoutPowerTarget(stepDTO._power)
        } else if (stepDTO._hr != null) {
            toWorkoutHrTarget(stepDTO._hr)
        } else if (stepDTO._pace != null) {
            toWorkoutPaceTarget(stepDTO._pace)
        } else {
            throw IntervalsException("Unknown target in step $stepDTO")
        }
    }

    fun mapCadenceTarget(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): WorkoutStepTarget {
        val (min, max) = if (stepValueDTO.value != null) {
            stepValueDTO.value to stepValueDTO.value
        } else {
            stepValueDTO.start!! to stepValueDTO.end!!
        }
        return WorkoutStepTarget(WorkoutStepTarget.TargetUnit.RPM, min, max)
    }

    private fun toWorkoutPowerTarget(power: IntervalsWorkoutDocDTO.ResolvedStepValueDTO): WorkoutStepTarget {
        return mapSimpleUnit(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, ftp!!, power)
    }

    private fun toWorkoutHrTarget(hr: IntervalsWorkoutDocDTO.ResolvedStepValueDTO): WorkoutStepTarget {
        return mapSimpleUnit(WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE, lthr!!, hr)
    }

    private fun toWorkoutPaceTarget(pace: IntervalsWorkoutDocDTO.ResolvedStepValueDTO): WorkoutStepTarget {
//        return mapSimpleUnit(WorkoutStepTarget.TargetUnit.PACE_PERCENTAGE, paceThreshold!!, pace)
        throw IntervalsException("Can't handle steps with pace target yet")
    }

    private fun mapSimpleUnit(
        targetUnit: WorkoutStepTarget.TargetUnit,
        thresholdValue: Double,
        resolvedStepValueDTO: IntervalsWorkoutDocDTO.ResolvedStepValueDTO
    ): WorkoutStepTarget {
        val (min, max) = getRange(thresholdValue, resolvedStepValueDTO)
        return WorkoutStepTarget(targetUnit, min, max)
    }

    private fun getRange(
        thresholdValue: Double,
        resolvedStepValueDTO: IntervalsWorkoutDocDTO.ResolvedStepValueDTO
    ): Pair<Int, Int> {
        val rangeStart = Math.round((resolvedStepValueDTO.start / thresholdValue) * 100).toInt()
        val rangeEnd = Math.round((resolvedStepValueDTO.end / thresholdValue) * 100).toInt()
        return rangeStart to rangeEnd
    }
}
