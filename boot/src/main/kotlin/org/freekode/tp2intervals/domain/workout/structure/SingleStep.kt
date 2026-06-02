package org.freekode.tp2intervals.domain.workout.structure

import org.freekode.tp2intervals.utils.RampConverter

class SingleStep(
    val name: String?,
    val notes: String?,
    val length: StepLength,
    val target: StepTarget,
    val cadence: StepTarget?,
    val ramp: Boolean
) : WorkoutStep {

    constructor(name: String?,
                length: StepLength,
                target: StepTarget,
                cadence: StepTarget?,
                ramp: Boolean) : this(name, null, length, target, cadence, ramp)

    override fun isSingleStep() = true

    fun convertRampToMultiStep(): MultiStep {
        if (!ramp) {
            throw IllegalStateException("Step is not ramp step")
        }
        return RampConverter(this).toRampToMultiStep()
    }
}
