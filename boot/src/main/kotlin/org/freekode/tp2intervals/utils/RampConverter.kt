package org.freekode.tp2intervals.utils

import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import java.time.Duration

class RampConverter(
    private val workout: WorkoutSingleStep
) {
    fun toRampToMultiStep(): WorkoutMultiStep {
        val rampStepDuration = getRampStepDuration()
        val steps = mutableListOf<WorkoutSingleStep>()
        val stepsAmount = getRampSteps(rampStepDuration)

        var remainedSeconds = workout.length.value
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
                    workout.cadence,
                    false
                )
            )
            remainedSeconds -= rampStepDuration
        }

        return WorkoutMultiStep(workout.name, 1, steps)
    }

    private fun getRampSteps(stepDuration: Int): Int {
        val stepsAmount = (workout.length.value / stepDuration).toInt()
        if (workout.length.value % stepDuration > 0) {
            return stepsAmount + 1
        }
        return stepsAmount
    }

    private fun getTargetForRampStep(stepNum: Int, stepsAmount: Int): StepTarget {
        val targetDiff = workout.target.end - workout.target.start
        val stepTargetDiff = targetDiff / stepsAmount

        val stepStart = workout.target.start + stepTargetDiff * stepNum
        val stepEnd = stepStart + stepTargetDiff
        return StepTarget(stepStart, stepEnd)
    }

    private fun getRampStepDuration(): Int {
        return if (Duration.ofSeconds(workout.length.value).toMinutes() in 1..10) {
            60
        } else if (Duration.ofSeconds(workout.length.value).toMinutes() in 11..15) {
            60 * 2
        } else {
            60 * 3
        }
    }
}
