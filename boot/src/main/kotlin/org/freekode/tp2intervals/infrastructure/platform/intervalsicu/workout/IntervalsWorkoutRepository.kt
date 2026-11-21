package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import feign.FeignException
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
import org.freekode.tp2intervals.infrastructure.ProviderReadFailure
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    fun resolveCalendarIdentity() = intervalsConfigurationRepository.resolveCalendarIdentity()

    fun createScopedCalendarEvent(
        identity: IntervalsConfigurationRepository.CalendarIdentity, sourceAccountId: String, workout: Workout,
    ): Long = confirmedId(intervalsApiClient.createScheduledEvent(
        identity.athleteId, identity.authorization, ToIntervalsWorkoutConverter().createEventRequestDTO(workout, sourceAccountId)
    ))

    private fun confirmedId(response: ResponseEntity<CreatedIntervalsEvent>): Long {
        return response.body?.id?.takeIf { response.statusCode.is2xxSuccessful && it > 0 }
            ?: throw PlatformException(Platform.INTERVALS, "Calendar create did not confirm an event ID")
    }

    fun getCalendarEvent(athleteId: String, eventId: Long): IntervalsEventDTO? {
        val response = try {
            intervalsApiClient.getEvent(athleteId, eventId)
        } catch (error: FeignException) {
            if (error.status() == HttpStatus.NOT_FOUND.value()) return null
            throw error
        }
        if (response.statusCode == HttpStatus.NOT_FOUND) return null
        return response.body?.takeIf { response.statusCode.is2xxSuccessful && it.id == eventId }
            ?: throw calendarReadFailure(response.statusCode.value())
    }

    fun getCalendarSnapshot(athleteId: String, startDate: LocalDate, endDate: LocalDate): List<IntervalsEventDTO> {
        val response = intervalsApiClient.getCalendarSnapshot(athleteId, startDate.toString(), endDate.toString())
        return response.body?.takeIf { response.statusCode.is2xxSuccessful }
            ?: throw calendarReadFailure(response.statusCode.value())
    }

    private fun calendarReadFailure(status: Int) = ProviderReadFailure(
        Platform.INTERVALS,
        if (status in 200..299) ProviderReadFailure.Kind.RESPONSE else ProviderReadFailure.Kind.HTTP,
        status,
    )

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
