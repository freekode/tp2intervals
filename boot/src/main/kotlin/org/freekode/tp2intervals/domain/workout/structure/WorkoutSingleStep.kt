package org.freekode.tp2intervals.domain.workout.structure

import org.freekode.tp2intervals.utils.RampConverter
import java.time.Duration

class WorkoutSingleStep(
    val name: String?,
    val length: StepLength,
    val target: StepTarget,
    val cadence: StepTarget?,
    val ramp: Boolean
) : WorkoutStep {
    @Deprecated("use default")
    constructor(
        name: String?,
        duration: Duration,
        target: StepTarget,
        cadence: StepTarget?,
        ramp: Boolean
    ) : this(name, StepLength.seconds(duration.seconds), target, cadence, ramp)

    override fun isSingleStep() = true

    fun convertRampToMultiStep(): WorkoutMultiStep {
        if (!ramp) {
            throw IllegalStateException("Step is not ramp step")
        }
        return RampConverter(this).toRampToMultiStep()
    }
}
