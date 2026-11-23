package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import config.TestUtils
import config.mock.IntervalsApiClientMock
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class IntervalsWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val intervalsApiClient: IntervalsApiClient = IntervalsApiClientMock(
        objectMapper,
        ResourceUtils.getFile("classpath:intervals-events-response.json").inputStream()
    )

    private val intervalsConfigurationRepository: IntervalsConfigurationRepository =
        getIntervalsConfigurationRepository()
    private val trackingRepository = mock(TrainerRoadSyncedEventRepository::class.java)

    private val intervalsWorkoutRepository =
        IntervalsWorkoutRepository(intervalsApiClient, intervalsConfigurationRepository, trackingRepository)

    @Test
    fun `should parse hr workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertTrue(workouts.isNotEmpty())

        val workout = findWorkoutWithName("hr test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.LTHR_PERCENTAGE, structure.target)
        assertEquals(5, structure.steps.size)
        // 10m 50-70% LTHR
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(50, (structure.steps[0] as SingleStep).target.start)
        assertEquals(70, (structure.steps[0] as SingleStep).target.end)
        // 10m 80% LTHR
        assertEquals(78, (structure.steps[1] as SingleStep).target.start)
        assertEquals(82, (structure.steps[1] as SingleStep).target.end)
        // 10m ramp 45-60% LTHR
        assertEquals(45, (structure.steps[2] as SingleStep).target.start)
        assertEquals(60, (structure.steps[2] as SingleStep).target.end)
        (structure.steps[2] as SingleStep).ramp
        // 10m Z2 HR
        assertEquals(68, (structure.steps[3] as SingleStep).target.start)
        assertEquals(82, (structure.steps[3] as SingleStep).target.end)
        // 10m Z3-Z4 HR
        assertEquals(83, (structure.steps[4] as SingleStep).target.start)
        assertEquals(105, (structure.steps[4] as SingleStep).target.end)
    }

    @Test
    fun `should parse power workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertTrue(workouts.isNotEmpty())


        val workout = findWorkoutWithName("power test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(5, structure.steps.size)
        // 10m 10-30%
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(10, (structure.steps[0] as SingleStep).target.start)
        assertEquals(30, (structure.steps[0] as SingleStep).target.end)
        // 10m 40% 30-90rpm
        assertEquals(39, (structure.steps[1] as SingleStep).target.start)
        assertEquals(41, (structure.steps[1] as SingleStep).target.end)
        assertEquals(30, (structure.steps[1] as SingleStep).cadence!!.start)
        assertEquals(90, (structure.steps[1] as SingleStep).cadence!!.end)
        // 10m ramp 10-60%
        assertEquals(10, (structure.steps[2] as SingleStep).target.start)
        assertEquals(60, (structure.steps[2] as SingleStep).target.end)
        (structure.steps[2] as SingleStep).ramp
        // 10m Z2 85rpm
        assertEquals(56, (structure.steps[3] as SingleStep).target.start)
        assertEquals(75, (structure.steps[3] as SingleStep).target.end)
        assertEquals(85, (structure.steps[3] as SingleStep).cadence!!.start)
        assertEquals(85, (structure.steps[3] as SingleStep).cadence!!.end)
        // 10m Z3-Z4
        assertEquals(76, (structure.steps[4] as SingleStep).target.start)
        assertEquals(105, (structure.steps[4] as SingleStep).target.end)
    }

    @Test
    fun `should parse pace workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertTrue(workouts.isNotEmpty())


        val workout = findWorkoutWithName("pace test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.RUN, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, structure.target)
        assertEquals(4, structure.steps.size)
        // 10m 70% Pace
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(68, (structure.steps[0] as SingleStep).target.start)
        assertEquals(72, (structure.steps[0] as SingleStep).target.end)
        // 10m 80-110% Pace
        assertEquals(80, (structure.steps[1] as SingleStep).target.start)
        assertEquals(110, (structure.steps[1] as SingleStep).target.end)
        // 10m Z2 Pace
        assertEquals(79, (structure.steps[2] as SingleStep).target.start)
        assertEquals(88, (structure.steps[2] as SingleStep).target.end)
        // 10m Z3-Z5 Pace
        assertEquals(89, (structure.steps[3] as SingleStep).target.start)
        assertEquals(103, (structure.steps[3] as SingleStep).target.end)
    }

    @Test
    fun `should parse pace workout with distance based steps`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertTrue(workouts.isNotEmpty())

        val workout = findWorkoutWithName("distance test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.SWIM, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, structure.target)
        assertEquals(4, structure.steps.size)

        TestUtils.assertStep(structure.steps[0], 800, StepLength.LengthUnit.METERS, 20, 20)
        TestUtils.assertStep(structure.steps[1], 500, StepLength.LengthUnit.METERS, 70, 70)
        TestUtils.assertStep(structure.steps[2], 600, StepLength.LengthUnit.SECONDS, 20, 20)
        TestUtils.assertStep(structure.steps[3], 300, StepLength.LengthUnit.METERS, 100, 100)
    }

    @Test
    fun `should parse virtual ride workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertTrue(workouts.isNotEmpty())


        val workout = findWorkoutWithName("virtual ride test", workouts)
        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertEquals(5, workout.structure!!.steps.size)
    }

    @Test
    fun `should parse other workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertTrue(workouts.isNotEmpty())


        val workout = findWorkoutWithName("other test", workouts)
        assertEquals(TrainingType.UNKNOWN, workout.details.type)
        assertEquals(Duration.ofMinutes(45), workout.details.duration)
        assertEquals(32, workout.details.load)
        assertEquals(null, workout.structure)
    }

    @Test
    fun `deletes only an identified TrainerRoad event`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        val source = trainerRoadWorkout("Original")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))
        val tracked = markTracked()

        repository.deleteSyncedWorkout(target)

        assertEquals("42", api.deletedEventIds.single())
        verify(trackingRepository).delete(tracked)
    }

    @Test
    fun `refuses deletion without both IDs`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        val source = trainerRoadWorkout("Workout")
        val missingTrainerRoadId = source.copy(details = source.details.copy(externalData = ExternalData.empty().withIntervals("42")))
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.deleteSyncedWorkout(missingTrainerRoadId)
        }
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.deleteSyncedWorkout(source)
        }
        assertTrue(api.deletedEventIds.isEmpty())
    }

    @Test
    fun `delete failure propagates`() {
        val api = RecordingClient(failDelete = true)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))
        markTracked()
        assertThrows(IllegalStateException::class.java) {
            repository.deleteSyncedWorkout(target)
        }
    }

    @Test
    fun `updates a matching event in place preserving time when no default is specified`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        val source = trainerRoadWorkout("New")
        val target = source.copy(
            startTime = LocalTime.of(18, 30),
            details = source.details.copy(externalData = source.details.externalData.withIntervals("42")),
        )
        markTracked()

        repository.updateSyncedWorkout(source, target)

        val (id, request) = api.updatedRequests.single()
        assertEquals("42", id)
        assertEquals("New", request.name)
        assertEquals("2026-11-23T18:30:00", request.start_date_local)
        assertTrue(api.deletedEventIds.isEmpty())
    }

    @Test
    fun `update requires matching IDs and a confirmed event response`() {
        val api = RecordingClient(confirmUpdate = false)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))
        markTracked()
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.updateSyncedWorkout(source.copy(date = source.date!!.plusDays(1)), target)
        }
        assertTrue(api.updatedRequests.isEmpty())
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.updateSyncedWorkout(source, target)
        }
        assertTrue(api.deletedEventIds.isEmpty())
    }

    @Test
    fun `untracked events cannot be updated or deleted even with a TrainerRoad ID`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))

        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.updateSyncedWorkout(source, target)
        }
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.deleteSyncedWorkout(target)
        }
        assertTrue(api.updatedRequests.isEmpty())
        assertTrue(api.deletedEventIds.isEmpty())
    }

    @Test
    fun `new TrainerRoad events are recorded against athlete event and source IDs`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)

        repository.saveTrainerRoadWorkoutsToCalendar(listOf(trainerRoadWorkout("Workout")))

        val captor = org.mockito.ArgumentCaptor.forClass(TrainerRoadSyncedEvent::class.java)
        verify(trackingRepository).save(captor.capture())
        assertEquals("athleteId", captor.value.athleteId)
        assertEquals("42", captor.value.eventId)
        assertEquals("123", captor.value.trainerRoadId)
    }

    @Test
    fun `missing create response fails without recording ownership`() {
        val api = RecordingClient(confirmCreate = false)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.saveTrainerRoadWorkoutsToCalendar(listOf(trainerRoadWorkout("Workout")))
        }
        verify(trackingRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `registration failure reports that created event remains protected`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository, trackingRepository)
        org.mockito.Mockito.doThrow(org.springframework.dao.DataIntegrityViolationException("database failure"))
            .`when`(trackingRepository).save(org.mockito.ArgumentMatchers.any())

        val error = assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.saveTrainerRoadWorkoutsToCalendar(listOf(trainerRoadWorkout("Workout")))
        }

        assertTrue(error.message!!.contains("created but could not be tracked"))
        assertTrue(api.deletedEventIds.isEmpty())
    }

    private class RecordingClient(
        private val failDelete: Boolean = false,
        private val confirmUpdate: Boolean = true,
        private val confirmCreate: Boolean = true,
    ) :
        IntervalsApiClient by mock(IntervalsApiClient::class.java) {
        val deletedEventIds = mutableListOf<String>()
        val updatedRequests = mutableListOf<Pair<String, CreateEventRequestDTO>>()

        override fun createEvent(athleteId: String, createEventRequestDTO: CreateEventRequestDTO): EventWriteResponseDTO? =
            if (confirmCreate) EventWriteResponseDTO(42) else null

        override fun deleteEvent(athleteId: String, eventId: String) {
            if (failDelete) throw IllegalStateException("delete failed")
            deletedEventIds.add(eventId)
        }

        override fun updateEvent(athleteId: String, eventId: String, request: CreateEventRequestDTO): EventWriteResponseDTO? {
            updatedRequests.add(eventId to request)
            return if (confirmUpdate) EventWriteResponseDTO(eventId.toLong()) else null
        }
    }

    private fun markTracked(): TrainerRoadSyncedEvent {
        val event = TrainerRoadSyncedEvent(1, "athleteId", "42", "123")
        `when`(trackingRepository.findByAthleteIdAndEventIdAndTrainerRoadId("athleteId", "42", "123")).thenReturn(event)
        return event
    }

    private fun trainerRoadWorkout(name: String) = Workout(
        WorkoutDetails(TrainingType.BIKE, name, null, null, null, ExternalData.empty().withTrainerRoad("123")),
        LocalDate.of(2026, 11, 23),
        null,
    )

    private fun findWorkoutWithName(name: String, workouts: List<Workout>): Workout {
        return workouts.find { it.details.name == name }!!
    }

    private fun getIntervalsConfigurationRepository(): IntervalsConfigurationRepository {
        val repo = mock(IntervalsConfigurationRepository::class.java)
        `when`(repo.getConfiguration()).thenReturn(IntervalsConfiguration("apiKey", "athleteId", 0.1f, 0.2f, 0.3f))
        return repo
    }
}
