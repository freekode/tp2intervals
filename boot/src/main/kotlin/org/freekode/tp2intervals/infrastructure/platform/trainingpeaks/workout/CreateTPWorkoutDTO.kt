package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.LocalDate
import java.time.LocalDateTime

class CreateTPWorkoutDTO(
    var athleteId: String,
    var workoutDay: LocalDateTime,
    var workoutTypeValueId: Int,
    var title: String,
    var totalTime: Double?,
    var totalTimePlanned: Double?,
    var tssActual: Double?,
    var tssPlanned: Double?,
    var structure: String?
) {
    companion object {
        fun planWorkout(
            athleteId: String, workoutDay: LocalDate, workoutTypeValueId: Int, title: String,
            totalTimePlanned: Double?, tssPlanned: Double?, structure: String?
        ): CreateTPWorkoutDTO {
            return CreateTPWorkoutDTO(
                athleteId,
                workoutDay.atStartOfDay(),
                workoutTypeValueId,
                title,
                null,
                totalTimePlanned,
                null,
                tssPlanned,
                structure
            )
        }

        fun createWorkout(
            athleteId: String, workoutDay: LocalDateTime, workoutTypeValueId: Int, title: String,
            totalTime: Double?, tssActual: Double?
        ): CreateTPWorkoutDTO {
            return CreateTPWorkoutDTO(
                athleteId,
                workoutDay,
                workoutTypeValueId,
                title,
                totalTime,
                null,
                tssActual,
                null,
                null
            )
        }
    }
}
