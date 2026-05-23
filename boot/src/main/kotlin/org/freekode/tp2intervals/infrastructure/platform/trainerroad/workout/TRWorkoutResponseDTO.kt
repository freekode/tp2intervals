package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.math.roundToInt

class TRWorkoutResponseDTO(
    @JsonProperty("Workout")
    @JsonAlias("workout")
    val workout: TRWorkout,
) {
    class TRWorkout(
        @JsonProperty("Details")
        @JsonAlias("details")
        val details: TrainerRoadWorkoutDetailsDTO,
        @JsonProperty("IntervalData")
        @JsonAlias("intervalData")
        val intervalData: List<IntervalsDataDTO> = emptyList(),
    )

    class IntervalsDataDTO(
        @JsonProperty("Start")
        @JsonAlias("start")
        val start: Double,
        @JsonProperty("End")
        @JsonAlias("end")
        val end: Double,
        @JsonProperty("Name")
        @JsonAlias("name")
        val name: String,
        @JsonProperty("IsFake")
        @JsonAlias("isFake")
        val isFake: Boolean,
        @JsonProperty("TestInterval")
        @JsonAlias("testInterval")
        val testInterval: Boolean,
        @JsonProperty("StartTargetPowerPercent")
        @JsonAlias("startTargetPowerPercent")
        val startTargetPowerPercent: Double = 0.0,
        @JsonProperty("StartTarget")
        @JsonAlias("startTarget")
        val startTarget: List<Double>? = null,
    ) {
        fun targetStart(): Int = (startTarget?.firstOrNull() ?: startTargetPowerPercent).roundToInt()

        fun targetEnd(): Int = (startTarget?.getOrNull(1) ?: targetStart().toDouble()).roundToInt()
    }
}
