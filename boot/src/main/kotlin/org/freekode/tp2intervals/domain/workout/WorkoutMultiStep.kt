package org.freekode.tp2intervals.domain.workout

class WorkoutMultiStep(
    val title: String,
    val repetitions: Int,
    val steps: List<WorkoutSingleStep>
) : WorkoutStep {
    override fun isSingleStep() = false
}
