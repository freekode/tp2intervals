package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

class IntervalsWorkoutDocDTO(
    val steps: List<WorkoutStepDTO>,
    val duration: Long?,
) {
    class WorkoutStepDTO(
        val reps: Int?,
        val text: String?,
        val distance: Int?,
        val duration: Long?,
        val power: StepValueDTO?,
        val cadence: StepValueDTO?,
        val steps: List<WorkoutStepDTO>?,
        val warmup: Boolean?,
        val cooldown: Boolean?
    )

    class StepValueDTO(
        val units: String,
        val value: Int?,
        val start: Int?,
        val end: Int?,
    )
}
