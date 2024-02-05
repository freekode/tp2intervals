package org.freekode.tp2intervals.infrastructure.trainerroad.activity

import java.time.LocalDateTime

class TrainerRoadActivityDTO(
    val Id: String,
    val Date: LocalDateTime,
    val CompletedRide: CompletedRideDTO
) {
    class CompletedRideDTO(
        val Name: String,
        val Date: LocalDateTime,
        val IsOutside: Boolean,
        val Tss: Long,
        val EstimatedDuration: Long,
        val Duration: Long,
        val Distance: Double,
        val WorkoutRecordId: Long
    )
}
