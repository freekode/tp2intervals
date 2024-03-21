package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

class TrainerRoadActivityDTO(
    val Id: String,
    @JsonProperty("Date")
    val date: LocalDateTime,
    @JsonProperty("CompletedRide")
    val completedRide: CompletedRideDTO?,
    @JsonProperty("Activity")
    val activity: ActivityDTO?
) {
    class ActivityDTO(
        @JsonProperty("Id")
        val id: String,
    )

    class CompletedRideDTO(
        val Name: String,
        val Date: LocalDateTime,
        val IsOutside: Boolean,
        val Tss: Int,
        val EstimatedDuration: Long,
        val Duration: Long,
        val Distance: Double,
        val WorkoutRecordId: Long
    )
}
