package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.math.absoluteValue

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
) : WorkoutRepository {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun platform() = Platform.INTERVALS

    override fun planWorkout(workout: Workout) {
        val workoutString = getWorkoutString(workout)

        var description = workout.description.orEmpty()
        description += workoutString?.let { "\n\n- - - -\n$it" }.orEmpty()

        val request = CreateEventRequestDTO(
            workout.date.atStartOfDay().toString(),
            workout.name,
            workout.type.title,
            "WORKOUT",
            description
        )
        intervalsApiClient.createEvent(intervalsConfigurationRepository.getConfiguration().athleteId, request)
    }

    override fun saveWorkout(workout: Workout, plan: Plan) {
        val workoutString = getWorkoutString(workout)

        var description = workout.description.orEmpty()
        description += workoutString?.let { "\n\n- - - -\n$it" }.orEmpty()

        val request = CreateWorkoutRequestDTO(
            plan.id.value,
            getWorkoutDayNumber(plan.startDate, workout.date),
            IntervalsEventTypeMapper.getByTrainingType(workout.type),
            workout.name,
            workout.duration?.seconds,
            workout.load,
            description,
            null,
        )
        intervalsApiClient.createWorkout(intervalsConfigurationRepository.getConfiguration().athleteId, request)
    }

    override fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val configuration = intervalsConfigurationRepository.getConfiguration()
        val events = intervalsApiClient.getEvents(
            configuration.athleteId,
            startDate.toString(),
            endDate.toString(),
            configuration.powerRange,
            configuration.hrRange,
            configuration.paceRange,
        )
        return events
            .filter { it.isWorkout() }
            .mapNotNull { toWorkout(it) }
    }

    override fun getWorkout(id: String): Workout {
        TODO("Not yet implemented")
    }

    private fun getWorkoutString(workout: Workout) =
        if (workout.structure != null) {
            StructureToIntervalsConverter(workout.structure).toIntervalsStructureStr()
        } else {
            null
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
