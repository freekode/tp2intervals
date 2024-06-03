package org.freekode.tp2intervals.domain.workout.structure

import java.time.Duration

class WorkoutSingleStep(
    val name: String?,
    val length: StepLength,
    @Deprecated("use length")
    val duration: Duration,
    val target: StepTarget,
    val cadence: StepTarget?,
    val ramp: Boolean
) : WorkoutStep {
    constructor(
        name: String?,
        duration: Duration,
        target: StepTarget,
        cadence: StepTarget?,
        ramp: Boolean
    ) : this(name, StepLength.seconds(duration.seconds), duration, target, cadence, ramp)

    override fun isSingleStep() = true

    fun convertRampToMultiStep(): WorkoutMultiStep {
        if (!ramp) {
            throw IllegalStateException("Step is not ramp step")
        }
        return RampConverter(this).tRampMultiStep()
    }
}
