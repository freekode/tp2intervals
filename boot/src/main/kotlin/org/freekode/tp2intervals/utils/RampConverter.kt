package org.freekode.tp2intervals.utils

import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.freekode.tp2intervals.domain.workout.structure.MultiStep
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import java.time.Duration

class RampConverter(
    private val step: SingleStep
) {
    fun toRampToMultiStep(): MultiStep {
        if (step.length.unit != StepLength.LengthUnit.SECONDS) {
            throw IllegalStateException("Ramp converter can only be converted with time based step")
        }

        val rampStepDuration = getRampStepDuration()
        val steps = mutableListOf<SingleStep>()
        val stepsAmount = getRampSteps(rampStepDuration)

        var remainedSeconds = step.length.value
        for (i in 0 until stepsAmount) {
            val stepDuration = if (remainedSeconds >= rampStepDuration) {
                rampStepDuration
            } else {
                remainedSeconds
            }

            steps.add(
                SingleStep(
                    "Ramp",
                    StepLength(stepDuration.toLong(), StepLength.LengthUnit.SECONDS),
                    getTargetForRampStep(i, stepsAmount),
                    step.cadence,
                    false
                )
            )
            remainedSeconds -= rampStepDuration
        }

        return MultiStep(step.name, 1, steps)
    }

    private fun getRampSteps(stepDuration: Int): Int {
        val stepsAmount = (step.length.value / stepDuration).toInt()
        if (step.length.value % stepDuration > 0) {
            return stepsAmount + 1
        }
        return stepsAmount
    }

    private fun getTargetForRampStep(stepNum: Int, stepsAmount: Int): StepTarget {
        val targetDiff = step.target.end - step.target.start
        val stepTargetDiff = targetDiff / stepsAmount

        val stepStart = step.target.start + stepTargetDiff * stepNum
        val stepEnd = stepStart + stepTargetDiff
        return StepTarget(stepStart, stepEnd)
    }

    private fun getRampStepDuration(): Int {
        return if (Duration.ofSeconds(step.length.value).toMinutes() in 1..10) {
            60
        } else if (Duration.ofSeconds(step.length.value).toMinutes() in 11..15) {
            60 * 2
        } else {
            60 * 3
        }
    }
}
