package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

class CreateEventRequestDTO(
    val start_date_local: String?,
    val name: String?,
    val type: String?,
    val category: String?,
    val description: String?,

    val moving_time: Long?,
    val icu_training_load: Int?,
)
