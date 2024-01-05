package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.plan.Folder
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val appConfigRepository: AppConfigRepository,
    private val intervalsMapper: IntervalsMapper,
    private val intervalsWorkoutDocMapper: IntervalsWorkoutStepMapper
) {

    fun createAndPlanWorkout(folder: Folder, workout: Workout) {
        val workoutString = intervalsWorkoutDocMapper.mapToIntervalsWorkout(workout)

        var description = workout.description.orEmpty()
        description += workoutString?.let { "\n\n- - - -\n$it" }.orEmpty()

        val createWorkoutRequestDTO = CreateWorkoutRequestDTO(
            folder.id.value,
            getWorkoutDayNumber(folder.startDate, workout.date),
            IntervalsWorkoutType.findByType(workout.type),
            workout.title,
            workout.duration?.seconds,
            workout.load?.toInt(),
            description,
            null,
        )
        intervalsApiClient.createWorkout(
            appConfigRepository.getConfig().intervalsConfig.athleteId,
            createWorkoutRequestDTO
        )
    }

    fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val events =
            intervalsApiClient.getEvents(
                appConfigRepository.getConfig().intervalsConfig.athleteId,
                startDate.toString(),
                endDate.toString()
            )
        return events
            .filter { it.category == IntervalsEventDTO.EventCategory.WORKOUT }
            .map { intervalsMapper.mapToWorkout(it) }
    }

    fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val activities =
            intervalsApiClient.getActivities(
                appConfigRepository.getConfig().intervalsConfig.athleteId,
                startDate.toString(),
                endDate.toString()
            )
        return activities
            .map { intervalsMapper.mapToActivity(it) }
    }

    private fun getWorkoutDayNumber(startDate: LocalDate, currentDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, currentDate).toInt().absoluteValue
    }

}
