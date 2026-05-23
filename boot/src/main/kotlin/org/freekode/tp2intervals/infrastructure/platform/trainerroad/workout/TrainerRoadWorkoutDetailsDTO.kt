package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

class TrainerRoadWorkoutDetailsDTO(
    @JsonProperty("Id")
    @JsonAlias("id")
    val id: String,
    @JsonProperty("WorkoutName")
    @JsonAlias("workoutName")
    val workoutName: String,
    @JsonProperty("WorkoutDescription")
    @JsonAlias("workoutDescription")
    val workoutDescription: String,
    @JsonProperty("IsOutside")
    @JsonAlias("isOutside")
    val isOutside: Boolean,
    @JsonProperty("Tss")
    @JsonAlias("tss")
    val tss: Int,
    @JsonProperty("Duration")
    @JsonAlias("duration")
    val duration: Int,
)
