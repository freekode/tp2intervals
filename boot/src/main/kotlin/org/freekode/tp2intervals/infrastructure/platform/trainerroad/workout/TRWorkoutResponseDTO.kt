package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonProperty
class TRWorkoutResponseDTO(
    @JsonProperty("Workout")
    val workout: TRWorkout,
) {
    class TRWorkout(
        @JsonProperty("Details")
        val details: TrainerRoadWorkoutDetailsDTO,
        @JsonProperty("IntervalData")
        val intervalData: List<IntervalsDataDTO> = emptyList(),
        @JsonProperty("WorkoutData")
        val workoutData: List<WorkoutDataDTO> = emptyList(),
    )

    class WorkoutDataDTO(
        @JsonProperty("Tick")
        val tick: Double,
        @JsonProperty("FtpPercent")
        val ftpPercent: Double,
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
        val startTargetPowerPercent: Double? = null,
        @JsonProperty("EndTargetPowerPercent")
        val endTargetPowerPercent: Double? = null,
        @JsonProperty("StartTarget")
        val startTarget: List<Double>? = null,
    )
}
