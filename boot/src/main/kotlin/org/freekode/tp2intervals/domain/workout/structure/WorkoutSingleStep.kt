package org.freekode.tp2intervals.domain.workout.structure

import java.time.Duration

class WorkoutSingleStep(
    val title: String,
    val duration: Duration,
    val target: WorkoutStepTarget,
    val cadence: WorkoutStepTarget?,
    val ramp: Boolean
) : WorkoutStep {
    override fun isSingleStep() = true

    fun convertRampToMultiStep(): WorkoutMultiStep {
        if (!ramp) {
            throw IllegalStateException("Step is not ramp step")
        }
        val rampStepDuration = getRampStepDuration()
        val steps = mutableListOf<WorkoutSingleStep>()
        val stepsAmount = getRampSteps(rampStepDuration)

        var remainedSeconds = duration.seconds
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
                    cadence,
                    false
                )
            )
            remainedSeconds -= rampStepDuration
        }

        return WorkoutMultiStep(title, 1, steps)
    }

    private fun getRampSteps(stepDuration: Int): Int {
        val stepsAmount = (duration.seconds / stepDuration).toInt()
        if (duration.seconds % stepDuration > 0) {
            return stepsAmount + 1
        }
        return stepsAmount
    }

    private fun getTargetForRampStep(stepNum: Int, stepsAmount: Int): WorkoutStepTarget {
        val targetDiff = target.end - target.start
        val stepTargetDiff = targetDiff / stepsAmount

        val stepStart = target.start + stepTargetDiff * stepNum
        val stepEnd = stepStart + stepTargetDiff
        return WorkoutStepTarget(stepStart, stepEnd)
    }

    private fun getRampStepDuration(): Int {
        return if (duration.toMinutes() in 1..10) {
            60
        } else if (duration.toMinutes() in 11..15) {
            60 * 2
        } else {
            60 * 3
        }
    }
}
