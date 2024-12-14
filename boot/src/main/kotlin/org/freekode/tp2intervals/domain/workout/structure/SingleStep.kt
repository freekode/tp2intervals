package org.freekode.tp2intervals.domain.workout.structure

import org.freekode.tp2intervals.utils.RampConverter

class SingleStep(
    val name: String?,
    val length: StepLength,
    val target: StepTarget,
    val cadence: StepTarget?,
    val ramp: Boolean
) : WorkoutStep {
    override fun isSingleStep() = true

    fun convertRampToMultiStep(): MultiStep {
        if (!ramp) {
            throw IllegalStateException("Step is not ramp step")
        }
        return RampConverter(this).toRampToMultiStep()
    }
}
