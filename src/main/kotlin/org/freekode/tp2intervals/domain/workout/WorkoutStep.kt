package org.freekode.tp2intervals.domain.workout

import java.time.Duration

data class WorkoutStep(
    val repetitions: Int,
    val duration: Duration,
    val targets: List<WorkoutStepTarget>,
    val steps: List<WorkoutStep>
) {

    fun isSingleStep() = repetitions == 1
}
