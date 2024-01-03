package org.freekode.tp2intervals.domain.workout

import java.time.Duration

class WorkoutSingleStep(
    val title: String,
    val duration: Duration,
    val target: WorkoutStepTarget,
    val cadence: WorkoutStepTarget?,
    val intensity: StepIntensityType,
) : WorkoutStep {
    override fun isSingleStep() = true
}
