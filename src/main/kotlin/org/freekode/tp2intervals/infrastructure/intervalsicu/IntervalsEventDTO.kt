package org.freekode.tp2intervals.infrastructure.intervalsicu

import java.time.LocalDateTime

class IntervalsEventDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val category: EventCategory,
    val type: IntervalsWorkoutType?,
    val moving_time: Long?,
    val icu_training_load: Int?,
    val workout_doc: WorkoutDocDTO?
) {
    enum class EventCategory {
        NOTE,
        WORKOUT
    }

    class WorkoutDocDTO(
        val steps: List<WorkoutStepDTO>,
        val duration: Long?,
    )

    class WorkoutStepDTO(
        val reps: Int?,
        val text: String?,
        val distance: Int?,
        val duration: Long?,
        val power: StepValueDTO?,
        val cadence: StepValueDTO?,
        val steps: List<WorkoutStepDTO>?
    )

    class StepValueDTO(
        val units: String,
        val value: Int?,
        val start: Int?,
        val end: Int?,
    )
}
