package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

class TrainerRoadActivityDTO(
    @JsonAlias("id")
    val Id: String,
    @JsonProperty("Date")
    @JsonAlias("date", "started")
    val date: LocalDateTime,
    @JsonProperty("CompletedRide")
    @JsonAlias("completedRide")
    val completedRide: CompletedRideDTO?,
    @JsonProperty("Activity")
    @JsonAlias("activity")
    val activity: ActivityDTO?,
    @JsonAlias("activityId")
    val activityId: Long?,
    @JsonAlias("name")
    val name: String?,
    @JsonAlias("isOutside")
    val isOutside: Boolean?,
    @JsonAlias("activityType")
    val activityType: Int?,
) {
    class ActivityDTO(
        @JsonProperty("Id")
        @JsonAlias("id")
        val id: String,
    )

    class CompletedRideDTO(
        @JsonAlias("name")
        val Name: String,
        @JsonAlias("date")
        val Date: LocalDateTime,
        @JsonAlias("isOutside")
        val IsOutside: Boolean,
        @JsonAlias("tss")
        val Tss: Int,
        @JsonAlias("estimatedDuration")
        val EstimatedDuration: Long,
        @JsonAlias("duration")
        val Duration: Long,
        @JsonAlias("distance")
        val Distance: Double,
        @JsonAlias("workoutRecordId")
        val WorkoutRecordId: Long
    )
}
