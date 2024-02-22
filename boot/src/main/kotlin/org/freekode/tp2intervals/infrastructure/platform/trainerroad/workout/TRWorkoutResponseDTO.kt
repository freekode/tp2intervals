package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonProperty

class TRWorkoutResponseDTO(
    @JsonProperty("Workout")
    val workout: TRWorkout,
) {
    class TRWorkout(
        @JsonProperty("Details")
        val details: DetailsDTO,
        val intervalData: List<IntervalsDataDTO>,
    )

    class DetailsDTO(
        @JsonProperty("Id")
        val id: Double,
        @JsonProperty("WorkoutName")
        val workoutName: String,
        @JsonProperty("WorkoutDescription")
        val workoutDescription: String,
        @JsonProperty("IsOutside")
        val isOutside: Boolean,
        @JsonProperty("TSS")
        val tss: Int,
        @JsonProperty("Duration")
        val duration: Int,
    )

    class IntervalsDataDTO(
        @JsonProperty("Start")
        val start: Double,
        @JsonProperty("End")
        val end: Double,
        @JsonProperty("Name")
        val name: String,
        @JsonProperty("IsFake")
        val isFake: Boolean,
        @JsonProperty("TestInterval")
        val testInterval: Boolean,
        @JsonProperty("StartTargetPowerPercent")
        val startTargetPowerPercent: Int,
    )
}
