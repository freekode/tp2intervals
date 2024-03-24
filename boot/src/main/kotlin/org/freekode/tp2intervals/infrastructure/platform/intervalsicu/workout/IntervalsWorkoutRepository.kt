package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.Signature
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
    private val maxWorkoutsToSave = 10

    override fun platform() = Platform.INTERVALS

    override fun saveWorkoutToCalendar(workout: Workout) {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)

        val request = CreateEventRequestDTO(
            (workout.date ?: LocalDate.now()).atStartOfDay().toString(),
            workout.details.name,
            workout.details.type.title,
            "WORKOUT",
            description
        )
        intervalsApiClient.createEvent(intervalsConfigurationRepository.getConfiguration().athleteId, request)
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        val workoutToIntervalsConverter = WorkoutToIntervalsConverter()
        for (fromIndex in 0..workouts.size step maxWorkoutsToSave) {
            val toIndex = if (fromIndex + maxWorkoutsToSave >= workouts.size) workouts.size else fromIndex + maxWorkoutsToSave

            val workoutsToSave = workouts.subList(fromIndex, toIndex)
            val requests =
                workoutsToSave.map { workoutToIntervalsConverter.createWorkoutRequestDTO(libraryContainer, it) }
            intervalsApiClient.createWorkouts(intervalsConfigurationRepository.getConfiguration().athleteId, requests)
        }
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
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

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        TODO("Not yet implemented")
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        TODO("Not yet implemented")
    }

    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        TODO("Not yet implemented")
    }

    private fun createWorkoutRequestDTO(libraryContainer: LibraryContainer, workout: Workout): CreateWorkoutRequestDTO {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)
        val request = CreateWorkoutRequestDTO(
            libraryContainer.externalData.intervalsId.toString(),
            Date.daysDiff(libraryContainer.startDate, workout.date ?: LocalDate.now()),
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            workout.details.name, // "Name is too long"
            workout.details.duration?.seconds,
            workout.details.load,
            description,
            null,
        )
        return request
    }

    private fun getDescription(workout: Workout, workoutString: String?): String {
        var description = workout.details.description
            .orEmpty()
            .replace(unwantedStepRegex, "--")
            .let { "$it\n- - - -\n${Signature.description}" }
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
