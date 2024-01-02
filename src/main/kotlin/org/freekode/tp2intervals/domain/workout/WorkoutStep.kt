package org.freekode.tp2intervals.domain.workout

import java.time.Duration

data class WorkoutStep(
    val title: String,
    val repetitions: Int,
    val duration: Duration,
    val targets: List<WorkoutStepTarget>,
    val intensity: StepIntensityType,
    val steps: List<WorkoutStep>
) {
    companion object {
        fun multiStep(title: String, repetitions: Int, steps: List<WorkoutStep>) =
            WorkoutStep(
                title,
                repetitions,
                Duration.ZERO,
                listOf(),
                StepIntensityType.default(),
                steps
            )

        fun singleStep(
            title: String, duration: Duration, targets: List<WorkoutStepTarget>, intensity: StepIntensityType,
        ) =
            WorkoutStep(title, 1, duration, targets, intensity, listOf())
    }

    fun isSingleStep() = repetitions == 1
}
