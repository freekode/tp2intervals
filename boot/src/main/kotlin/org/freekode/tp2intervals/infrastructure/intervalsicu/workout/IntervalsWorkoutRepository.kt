package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.intervalsicu.configuration.IntervalsConfigurationRepository
import org.springframework.stereotype.Repository
import kotlin.math.absoluteValue

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
    private val intervalsToWorkoutMapper: IntervalsToWorkoutMapper,
    private val intervalsWorkoutDocMapper: WorkoutToIntervalsMapper
) : WorkoutRepository {

    override fun planWorkout(workout: Workout, plan: Plan) {
        val workoutString = intervalsWorkoutDocMapper.mapToIntervalsWorkout(workout)

        var description = workout.description.orEmpty()
        description += workoutString?.let { "\n\n- - - -\n$it" }.orEmpty()

        val createWorkoutRequestDTO = CreateWorkoutRequestDTO(
            plan.id.value,
            getWorkoutDayNumber(plan.startDate, workout.date),
            IntervalsWorkoutType.findByType(workout.type),
            workout.title,
            workout.duration?.seconds,
            workout.load?.toInt(),
            description,
            null,
        )
        intervalsApiClient.createWorkout(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            createWorkoutRequestDTO
        )
    }

    override fun platform() = Platform.INTERVALS

    override fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val events =
            intervalsApiClient.getEvents(
                intervalsConfigurationRepository.getConfiguration().athleteId,
                startDate.toString(),
                endDate.toString()
            )
        return events
            .filter { it.category == IntervalsEventDTO.EventCategory.WORKOUT }
            .map { intervalsToWorkoutMapper.mapToWorkout(it) }
    }

    private fun getWorkoutDayNumber(startDate: LocalDate, currentDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, currentDate).toInt().absoluteValue
    }

}
