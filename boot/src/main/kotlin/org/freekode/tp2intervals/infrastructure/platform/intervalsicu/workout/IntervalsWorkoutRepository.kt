package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
) : WorkoutRepository {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val unwantedStepRegex = "^-".toRegex(RegexOption.MULTILINE)

    override fun platform() = Platform.INTERVALS

    override fun planWorkout(workout: Workout) {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)

        val request = CreateEventRequestDTO(
            workout.date.atStartOfDay().toString(),
            workout.name,
            workout.type.title,
            "WORKOUT",
            description
        )
        intervalsApiClient.createEvent(intervalsConfigurationRepository.getConfiguration().athleteId, request)
    }

    override fun saveWorkoutToPlan(workout: Workout, plan: Plan) {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)

        val request = CreateWorkoutRequestDTO(
            plan.externalData.intervalsId.toString(),
            Date.daysDiff(plan.startDate, workout.date),
            IntervalsTrainingTypeMapper.getByTrainingType(workout.type),
            workout.name, // "Name is too long"
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

    override fun getWorkoutsFromPlan(plan: Plan): List<Workout> {
        TODO("Not yet implemented")
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<Workout> {
        TODO("Not yet implemented")
    }

    private fun getDescription(workout: Workout, workoutString: String?): String {
        var description = workout.description
            .orEmpty()
            .replace(unwantedStepRegex, "--")
        description += workoutString
            ?.let { "\n\n- - - -\n$it" }
            .orEmpty()
        return description
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
}
