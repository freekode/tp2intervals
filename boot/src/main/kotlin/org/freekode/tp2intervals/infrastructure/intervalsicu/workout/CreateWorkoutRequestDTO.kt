package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

class CreateWorkoutRequestDTO(
    val folder_id: String,
    val day: Int,
    val type: IntervalsWorkoutType,
    val name: String?,
    val moving_time: Long?,
    val icu_training_load: Int?,
    val description: String?,
    val file_contents: String?,
)
