package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
) : WorkoutRepository {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val maxWorkoutsToSave = 10

    override fun platform() = Platform.INTERVALS

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        workouts.forEach {
            val toIntervalsWorkoutConverter = ToIntervalsWorkoutConverter()
            val request = toIntervalsWorkoutConverter.createEventRequestDTO(it)
            intervalsApiClient.createEvent(intervalsConfigurationRepository.getConfiguration().athleteId, request)
        }
    }

    override fun saveTrainerRoadWorkoutsToCalendar(workouts: List<Workout>) {
        if (workouts.any { (it.details.externalData.trainerRoadId?.toLongOrNull() ?: 0) <= 0 || it.date == null }) {
            throw PlatformException(Platform.TRAINER_ROAD, "Cannot save a workout without a valid TrainerRoad ID and date")
        }
        val athleteId = intervalsConfigurationRepository.getConfiguration().athleteId
        val converter = ToIntervalsWorkoutConverter()
        val requests = workouts.map { converter.createEventRequestDTO(it) }
        requests.forEach { request ->
            val response = intervalsApiClient.createEvent(athleteId, request)
            if (response == null || response.id <= 0) {
                throw PlatformException(Platform.INTERVALS, "Intervals.icu did not confirm the event ID; check the calendar before retrying")
            }
        }
    }

    override fun hasExplicitTrainerRoadId(workout: Workout): Boolean {
        return workout.details.externalData.hasClearTrainerRoadId(workout.details.description) &&
            !ExternalData.hasAmbiguousMetadata(workout.details.description)
    }

    override fun isTrainerRoadWorkoutUpToDate(source: Workout, target: Workout): Boolean {
        val date = target.date ?: return false
        val time = target.startTime ?: return false
        if (source.date != date || source.details.externalData.trainerRoadId != target.details.externalData.trainerRoadId) return false
        val withTrainingPeaksId = preserveTrainingPeaksId(source, target)
        val workout = if (source.startTime == null) withTrainingPeaksId.withStartTime(time) else withTrainingPeaksId
        val expected = ToIntervalsWorkoutConverter().createEventRequestDTO(workout)
        fun normalize(text: String) = text.replace("\r\n", "\n").replace("\r", "\n")
        return expected.name == target.details.name &&
            expected.type == IntervalsTrainingTypeMapper.getByTrainingType(target.details.type) &&
            java.time.LocalDateTime.parse(expected.start_date_local) == date.atTime(time) &&
            normalize(expected.description) == normalize(target.details.description.orEmpty())
    }

    override fun deleteSyncedWorkout(workout: Workout) {
        if (!hasExplicitTrainerRoadId(workout)) {
            throw PlatformException(Platform.INTERVALS, "Cannot delete an event without a clear TrainerRoad ID")
        }
        val eventId = workout.details.externalData.intervalsId?.takeIf { it.isNotBlank() }
            ?: throw PlatformException(Platform.INTERVALS, "Cannot delete a workout without an Intervals.icu event ID")
        intervalsApiClient.deleteEvent(intervalsConfigurationRepository.getConfiguration().athleteId, eventId)
    }

    override fun updateSyncedWorkout(source: Workout, target: Workout) {
        val sourceId = source.details.externalData.trainerRoadId
        if (sourceId.isNullOrBlank() || !hasExplicitTrainerRoadId(target) ||
            sourceId != target.details.externalData.trainerRoadId ||
            source.date == null || source.date != target.date) {
            throw PlatformException(Platform.INTERVALS, "Cannot update an event without a matching TrainerRoad ID and date")
        }
        val eventId = target.details.externalData.intervalsId?.takeIf { it.isNotBlank() }
            ?: throw PlatformException(Platform.INTERVALS, "Cannot update a workout without an Intervals.icu event ID")
        val withTrainingPeaksId = preserveTrainingPeaksId(source, target)
        val workout = if (source.startTime == null) withTrainingPeaksId.withStartTime(target.startTime) else withTrainingPeaksId
        val result = intervalsApiClient.updateEvent(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            eventId,
            ToIntervalsWorkoutConverter().createEventRequestDTO(workout),
        )
        if (result == null || result.id.toString() != eventId) {
            throw PlatformException(Platform.INTERVALS, "Intervals.icu did not confirm the event update for $eventId")
        }
    }

    private fun preserveTrainingPeaksId(source: Workout, target: Workout): Workout {
        val trainingPeaksId = target.details.externalData.trainingPeaksId ?: return source
        return source.copy(details = source.details.copy(
            externalData = source.details.externalData.withTrainingPeaks(trainingPeaksId),
        ))
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        val toIntervalsWorkoutConverter = ToIntervalsWorkoutConverter()
        val requests = workouts.map { toIntervalsWorkoutConverter.createWorkoutRequestDTO(libraryContainer, it) }
        for (fromIndex in requests.indices step maxWorkoutsToSave) {
            val toIndex =
                if (fromIndex + maxWorkoutsToSave >= requests.size) requests.size else fromIndex + maxWorkoutsToSave
            intervalsApiClient.createWorkouts(intervalsConfigurationRepository.getConfiguration().athleteId, requests.subList(fromIndex, toIndex))
        }
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> =
        getCalendarWorkouts(startDate, endDate)

    override fun getWorkoutsForReconciliation(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val configuration = intervalsConfigurationRepository.getConfiguration()
        return intervalsApiClient.getEventsForReconciliation(configuration.athleteId, startDate.toString(), endDate.toString())
            .filter { it.isWorkout() }
            .map { event ->
                val externalData = ExternalData.empty().withIntervals(event.id.toString())
                    .fromSimpleString(event.description.orEmpty())
                Workout(
                    WorkoutDetails(event.mapType(), event.name, event.description, event.mapDuration(), event.icu_training_load, externalData),
                    event.start_date_local.toLocalDate(), null, event.start_date_local.toLocalTime(),
                )
            }
    }

    private fun getCalendarWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val configuration = intervalsConfigurationRepository.getConfiguration()
        val events = intervalsApiClient.getEvents(
            configuration.athleteId,
            startDate.toString(),
            endDate.toString(),
            configuration.powerRange,
            configuration.hrRange,
            configuration.paceRange,
        )
        return events.filter { it.isWorkout() }.mapNotNull { toWorkout(it) }
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

    override fun deleteWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate) {
        TODO("Not yet implemented")
    }

    private fun toWorkout(eventDTO: IntervalsEventDTO): Workout? {
        return try {
            FromIntervalsWorkoutConverter(eventDTO).toWorkout()
        } catch (e: PlatformException) {
            log.warn("Can't convert a workout ${eventDTO.name} on ${eventDTO.start_date_local}, skipping...", e)
            return null
        }
    }
}
