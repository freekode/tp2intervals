package org.freekode.tp2intervals.domain.workout.structure

import java.time.Duration

class RampConverter(
    private val step: WorkoutSingleStep
) {
    private val duration: Long = step.length.value

    fun tRampMultiStep(): WorkoutMultiStep {
        val rampStepDuration = getRampStepDuration()
        val steps = mutableListOf<WorkoutSingleStep>()
        val stepsAmount = getRampSteps(rampStepDuration)

        var remainedSeconds = duration
        for (i in 0 until stepsAmount) {
            val stepDuration = if (remainedSeconds >= rampStepDuration) {
                rampStepDuration
            } else {
                remainedSeconds
            }

            steps.add(
                WorkoutSingleStep(
                    "Ramp",
                    Duration.ofSeconds(stepDuration.toLong()),
                    getTargetForRampStep(i, stepsAmount),
                    step.cadence,
                    false
                )
            )
            remainedSeconds -= rampStepDuration
        }

        return WorkoutMultiStep(step.name, 1, steps)
    }

    private fun getRampSteps(stepDuration: Int): Int {
        val stepsAmount = (duration / stepDuration).toInt()
        if (duration % stepDuration > 0) {
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
        return if ((duration / 60) in 1..10) {
            60
        } else if ((duration / 60) in 11..15) {
            60 * 2
        } else {
            60 * 3
        }
    }
}
