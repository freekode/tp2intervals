package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Duration
import java.time.LocalDateTime
import org.freekode.tp2intervals.domain.TrainingType

class IntervalsEventDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val category: String,
    val type: String?,
    val moving_time: Long?,
    val icu_training_load: Int?,
    val workout_doc: IntervalsWorkoutDocDTO?,
) {

    fun mapType(): TrainingType = type?.let { IntervalsTrainingTypeMapper.getByIntervalsType(it) } ?: TrainingType.UNKNOWN

    fun mapDuration(): Duration? = moving_time?.let { Duration.ofSeconds(it) }

    fun isWorkout() = category == "WORKOUT"
}

@JsonIgnoreProperties(ignoreUnknown = true)
class IntervalsEventIdentityDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val category: String,
    val type: String?,
    val moving_time: Long? = null,
    val icu_training_load: Int? = null,
) {
    fun mapType(): TrainingType = type?.let { IntervalsTrainingTypeMapper.getByIntervalsType(it) } ?: TrainingType.UNKNOWN
    fun mapDuration(): Duration? = moving_time?.let { Duration.ofSeconds(it) }
    fun isWorkout() = category == "WORKOUT"
}
