package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonProperty

class TrainerRoadWorkoutDetailsDTO(
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
    @JsonProperty("GoalDescription")
    val goalDescription: String? = null,
    @JsonProperty("ProfileName")
    val profileName: String? = null,
    @JsonProperty("WorkoutDifficultyRating")
    val workoutDifficultyRating: Int? = null,
    @JsonProperty("WorkoutLevel")
    val workoutLevel: Double? = null,
    @JsonProperty("Zones")
    val zones: List<TrainerRoadZoneDTO> = emptyList(),
    @JsonProperty("Progression")
    val progression: TrainerRoadProgressionDTO? = null,
    @JsonProperty("ProgressionLevel")
    val progressionLevel: Double? = null,
    @JsonProperty("Levels")
    val levels: List<TrainerRoadLevelDTO> = emptyList(),
)

class TrainerRoadZoneDTO(
    @JsonProperty("Description")
    val description: String?,
)

class TrainerRoadProgressionDTO(
    @JsonProperty("Text")
    val text: String?,
)

class TrainerRoadLevelDTO(
    @JsonProperty("Level")
    val level: Double?,
    @JsonProperty("IsPrimary")
    val isPrimary: Boolean?,
)
