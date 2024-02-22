package org.freekode.tp2intervals.domain.workout.structure

class WorkoutMultiStep(
    val title: String,
    val repetitions: Int,
    val steps: List<WorkoutSingleStep>
) : WorkoutStep {
    override fun isSingleStep() = false
}
