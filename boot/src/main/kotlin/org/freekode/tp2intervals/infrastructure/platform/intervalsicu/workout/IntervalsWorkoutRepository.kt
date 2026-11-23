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
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
    private val trainerRoadSyncedEventRepository: TrainerRoadSyncedEventRepository,
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
        if (workouts.any { it.details.externalData.trainerRoadId.isNullOrBlank() || it.date == null }) {
            throw PlatformException(Platform.TRAINER_ROAD, "Cannot track a workout without a TrainerRoad ID and date")
        }
        val athleteId = intervalsConfigurationRepository.getConfiguration().athleteId
        val converter = ToIntervalsWorkoutConverter()
        workouts.forEach { workout ->
            val response = intervalsApiClient.createEvent(athleteId, converter.createEventRequestDTO(workout))
            if (response == null || response.id <= 0) {
                throw PlatformException(Platform.INTERVALS, "Intervals.icu did not confirm the event ID; check the calendar before retrying")
            }
            try {
                trainerRoadSyncedEventRepository.save(
                    TrainerRoadSyncedEvent(null, athleteId, response.id.toString(), workout.details.externalData.trainerRoadId!!),
                )
            } catch (e: DataAccessException) {
                log.error("Failed to track created Intervals.icu event {}", response.id, e)
                throw PlatformException(Platform.INTERVALS, "Event ${response.id} was created but could not be tracked; it will remain protected")
            }
        }
    }

    override fun isTrackedTrainerRoadWorkout(workout: Workout): Boolean =
        findTrackedTrainerRoadWorkout(workout) != null

    private fun findTrackedTrainerRoadWorkout(workout: Workout): TrainerRoadSyncedEvent? {
        val external = workout.details.externalData
        val eventId = external.intervalsId?.takeIf { it.isNotBlank() } ?: return null
        val trainerRoadId = external.trainerRoadId?.takeIf { it.isNotBlank() } ?: return null
        return trainerRoadSyncedEventRepository.findByAthleteIdAndEventIdAndTrainerRoadId(
            intervalsConfigurationRepository.getConfiguration().athleteId, eventId, trainerRoadId,
        )
    }

    override fun deleteSyncedWorkout(workout: Workout) {
        val tracked = findTrackedTrainerRoadWorkout(workout)
            ?: throw PlatformException(Platform.INTERVALS, "Cannot delete an untracked TrainerRoad workout")
        val eventId = workout.details.externalData.intervalsId?.takeIf { it.isNotBlank() }
            ?: throw PlatformException(Platform.INTERVALS, "Cannot delete a workout without an Intervals.icu event ID")
        intervalsApiClient.deleteEvent(intervalsConfigurationRepository.getConfiguration().athleteId, eventId)
        trainerRoadSyncedEventRepository.delete(tracked)
    }

    override fun updateSyncedWorkout(source: Workout, target: Workout) {
        val sourceId = source.details.externalData.trainerRoadId
        if (sourceId.isNullOrBlank() || !isTrackedTrainerRoadWorkout(target) ||
            sourceId != target.details.externalData.trainerRoadId ||
            source.date == null || source.date != target.date) {
            throw PlatformException(Platform.INTERVALS, "Cannot update an event without a matching TrainerRoad ID and date")
        }
        val eventId = target.details.externalData.intervalsId?.takeIf { it.isNotBlank() }
            ?: throw PlatformException(Platform.INTERVALS, "Cannot update a workout without an Intervals.icu event ID")
        val workout = if (source.startTime == null) source.withStartTime(target.startTime) else source
        val result = intervalsApiClient.updateEvent(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            eventId,
            ToIntervalsWorkoutConverter().createEventRequestDTO(workout),
        )
        if (result == null || result.id.toString() != eventId) {
            throw PlatformException(Platform.INTERVALS, "Intervals.icu did not confirm the event update for $eventId")
        }
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        val toIntervalsWorkoutConverter = ToIntervalsWorkoutConverter()
        for (fromIndex in workouts.indices step maxWorkoutsToSave) {
            val toIndex =
                if (fromIndex + maxWorkoutsToSave >= workouts.size) workouts.size else fromIndex + maxWorkoutsToSave

            val workoutsToSave = workouts.subList(fromIndex, toIndex)
            val requests =
                workoutsToSave.map { toIntervalsWorkoutConverter.createWorkoutRequestDTO(libraryContainer, it) }
            intervalsApiClient.createWorkouts(intervalsConfigurationRepository.getConfiguration().athleteId, requests)
        }
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> =
        getCalendarWorkouts(startDate, endDate, strict = false)

    override fun getWorkoutsForReconciliation(startDate: LocalDate, endDate: LocalDate): List<Workout> =
        getCalendarWorkouts(startDate, endDate, strict = true)

    private fun getCalendarWorkouts(startDate: LocalDate, endDate: LocalDate, strict: Boolean): List<Workout> {
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
            .mapNotNull { if (strict) FromIntervalsWorkoutConverter(it).toWorkout() else toWorkout(it) }
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
