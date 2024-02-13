package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.intervalsicu.configuration.IntervalsConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.math.absoluteValue

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
    private val workoutToIntervalsConverter: WorkoutToIntervalsConverter
) : WorkoutRepository {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun planWorkout(workout: Workout) {
        val workoutString = workoutToIntervalsConverter.toIntervalsWorkout(workout)

        var description = workout.description.orEmpty()
        description += workoutString?.let { "\n\n- - - -\n$it" }.orEmpty()

        val request = CreateEventRequestDTO(
            workout.date.atStartOfDay().toString(),
            workout.title,
            workout.type.title,
            "WORKOUT",
            description
        )
        intervalsApiClient.createEvent(intervalsConfigurationRepository.getConfiguration().athleteId, request)
    }

    override fun copyWorkout(workout: Workout, plan: Plan) {
        val workoutString = workoutToIntervalsConverter.toIntervalsWorkout(workout)

        var description = workout.description.orEmpty()
        description += workoutString?.let { "\n\n- - - -\n$it" }.orEmpty()

        val request = CreateWorkoutRequestDTO(
            plan.id.value,
            getWorkoutDayNumber(plan.startDate, workout.date),
            IntervalsEventTypeMapper.getByTrainingType(workout.type),
            workout.title,
            workout.duration?.seconds,
            workout.load?.toInt(),
            description,
            null,
        )
        intervalsApiClient.createWorkout(intervalsConfigurationRepository.getConfiguration().athleteId, request)
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
            .mapNotNull { toWorkout(it) }
    }

    private fun toWorkout(eventDTO: IntervalsEventDTO): Workout? {
        return try {
            IntervalsToWorkoutConverter(eventDTO).toWorkout()
        } catch (e: PlatformException) {
            log.warn("Can't convert a workout ${eventDTO.name} on ${eventDTO.start_date_local}, skipping...", e)
            return null
        }
    }

    private fun getWorkoutDayNumber(startDate: LocalDate, currentDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, currentDate).toInt().absoluteValue
    }

}
