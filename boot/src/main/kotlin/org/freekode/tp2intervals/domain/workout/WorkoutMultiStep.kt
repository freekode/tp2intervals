package org.freekode.tp2intervals.domain.workout

class WorkoutMultiStep(
    val title: String,
    val repetitions: Int,
    val steps: List<WorkoutSingleStep>
) : WorkoutStep {
    companion object {
        fun multiStep(title: String, repetitions: Int, steps: List<WorkoutSingleStep>) =
            WorkoutMultiStep(title, repetitions, steps)
    }

    override fun isSingleStep() = false
}
