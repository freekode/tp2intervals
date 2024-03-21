package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonProperty

class TRFindWorkoutsResponseDTO(
    @JsonProperty("Workouts")
    val workouts: List<TRWorkout>,
) {
    class TRWorkout(
        @JsonProperty("Id")
        val id: String,
        @JsonProperty("WorkoutName")
        val workoutName: String,
        @JsonProperty("WorkoutDescription")
        val workoutDescription: String,
        @JsonProperty("IsOutside")
        val isOutside: Boolean,
        @JsonProperty("Tss")
        val tss: Int,
        @JsonProperty("Duration")
        val duration: Int,
    )
}
