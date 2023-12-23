package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.domain.plan.Folder
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutType
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsProperties: IntervalsProperties,
    private val intervalsWorkoutMapper: IntervalsWorkoutMapper
) {

    fun createAndPlanWorkout(folder: Folder, workout: Workout) {
        val createWorkoutRequestDTO = CreateWorkoutRequestDTO(
            folder.id.value,
            getWorkoutDayNumber(folder.startDate, workout.scheduledDate),
            mapType(workout.type),
            workout.title,
            workout.duration?.seconds,
            workout.load?.toInt(),
            workout.description,
            workout.externalContent
        )
        intervalsApiClient.createWorkout(intervalsProperties.athleteId, createWorkoutRequestDTO)
    }

    fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val events =
            intervalsApiClient.getEvents(intervalsProperties.athleteId, startDate.toString(), endDate.toString())
        return events
            .filter { it.category == IntervalsEventDTO.EventCategory.WORKOUT }
            .map { intervalsWorkoutMapper.mapToWorkout(it) }
    }

    private fun mapType(type: WorkoutType): String {
        return when (type) {
            WorkoutType.BIKE -> "Ride"
            WorkoutType.RUN -> "Run"
            WorkoutType.WEIGHT -> "WeightTraining"
            WorkoutType.NOTE -> "NOTE"
            else -> "Other"
        }
    }

    private fun getWorkoutDayNumber(startDate: LocalDate, currentDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, currentDate).toInt().absoluteValue
    }

}
