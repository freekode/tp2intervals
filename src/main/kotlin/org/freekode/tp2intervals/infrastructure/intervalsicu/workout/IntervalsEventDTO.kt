package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

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
    val workout_doc: IntervalsWorkoutDocDTO?
) {
    enum class EventCategory {
        NOTE,
        WORKOUT
    }
}
