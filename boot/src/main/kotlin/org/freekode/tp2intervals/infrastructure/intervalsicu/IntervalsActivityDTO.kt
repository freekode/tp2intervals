package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsWorkoutType
import java.time.LocalDateTime

class IntervalsActivityDTO(
    val id: String,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val type: IntervalsWorkoutType?,
    val moving_time: Long?,
    val icu_training_load: Int?,
)
