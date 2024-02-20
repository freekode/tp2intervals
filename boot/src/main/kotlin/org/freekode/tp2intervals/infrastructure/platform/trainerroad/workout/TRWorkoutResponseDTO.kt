package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

class TRWorkoutResponseDTO(
    val Workout: TRWorkout,
) {
    class TRWorkout(
        val Details: DetailsDTO,
        val intervalData: List<IntervalsDataDTO>,
    )

    class DetailsDTO(
        val Id: Double,
        val WorkoutName: String,
        val WorkoutDescription: String,
        val IsOutside: Boolean,
        val TSS: Int,
        val Duration: Int,
    )

    class IntervalsDataDTO(
        val Start: Double,
        val End: Double,
        val Name: String,
        val IsFake: Boolean,
        val TestInterval: Boolean,
        val StartTargetPowerPercent: Int
    )
}
