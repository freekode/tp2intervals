package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStepTarget
import org.freekode.tp2intervals.infrastructure.PlatformException

class IntervalsToTargetConverter(
    private val ftp: Double?,
    private val lthr: Double?,
    private val paceThreshold: Double?
) {
    fun toMainTarget(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutStepTarget {
        return if (stepDTO._power != null) {
            mapSimpleUnit(ftp!!, stepDTO._power)
        } else if (stepDTO._hr != null) {
            mapSimpleUnit(lthr!!, stepDTO._hr)
        } else if (stepDTO._pace != null) {
            mapSimpleUnit(paceThreshold!!, stepDTO._pace)
        } else {
            throw PlatformException(Platform.INTERVALS, "Unknown target in step $stepDTO")
        }
    }

    fun toCadenceTarget(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): WorkoutStepTarget {
        val (min, max) = if (stepValueDTO.value != null) {
            stepValueDTO.value to stepValueDTO.value
        } else {
            stepValueDTO.start!! to stepValueDTO.end!!
        }
        return WorkoutStepTarget(min, max)
    }

    private fun mapSimpleUnit(
        thresholdValue: Double,
        resolvedStepValueDTO: IntervalsWorkoutDocDTO.ResolvedStepValueDTO
    ): WorkoutStepTarget {
        val (min, max) = getRange(thresholdValue, resolvedStepValueDTO)
        return WorkoutStepTarget(min, max)
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
