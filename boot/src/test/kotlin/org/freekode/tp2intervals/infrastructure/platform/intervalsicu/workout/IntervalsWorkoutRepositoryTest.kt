package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import config.TestUtils
import config.mock.IntervalsApiClientMock
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.domain.workout.TrainerRoadReconciliationSource
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.TrainerRoadSyncMode
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
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
    private val intervalsWorkoutRepository =
        IntervalsWorkoutRepository(intervalsApiClient, intervalsConfigurationRepository)

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
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Original")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))

        repository.deleteSyncedWorkout(target)

        assertEquals("42", api.deletedEventIds.single())
    }

    @Test
    fun `refuses deletion without both IDs`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
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
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))
        assertThrows(IllegalStateException::class.java) {
            repository.deleteSyncedWorkout(target)
        }
    }

    @Test
    fun `updates a matching event in place preserving time when no default is specified`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("New")
        val target = source.copy(
            startTime = LocalTime.of(18, 30),
            details = source.details.copy(externalData = source.details.externalData.withIntervals("42")),
        )

        repository.updateSyncedWorkout(sourceForWrite(source), target)

        val (id, request) = api.updatedRequests.single()
        assertEquals("42", id)
        assertEquals("New", request.name)
        assertEquals("2026-11-23T18:30:00", request.start_date_local)
        assertTrue(api.deletedEventIds.isEmpty())
    }

    @Test
    fun `update preserves an existing midnight time when no default is specified`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(
            startTime = LocalTime.MIDNIGHT,
            details = source.details.copy(externalData = source.details.externalData.withIntervals("42")),
        )

        repository.updateSyncedWorkout(sourceForWrite(source), target)
        repository.updateSyncedWorkout(sourceForWrite(source).withStartTime(LocalTime.MIDNIGHT), target)

        assertEquals("2026-11-23T00:00:00", api.updatedRequests[0].second.start_date_local)
        assertEquals("2026-11-23T00:00:00", api.updatedRequests[1].second.start_date_local)
    }

    @Test
    fun `comparison checks rendered workout content and preserves existing time by default`() {
        val repository = IntervalsWorkoutRepository(RecordingClient(), intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Workout").copy(
            details = trainerRoadWorkout("Workout").details.copy(description = "Description"),
            structure = WorkoutStructure(
                WorkoutStructure.TargetUnit.FTP_PERCENTAGE,
                listOf(SingleStep("Warmup", StepLength.seconds(300), StepTarget(50, 50), null, false)),
            ),
        )
        val time = LocalTime.of(18, 30)
        val rendered = ToIntervalsWorkoutConverter().createEventRequestDTO(source.withStartTime(time))
        val target = source.copy(
            details = source.details.copy(
                description = rendered.description.replace("\n", "\r\n"),
                externalData = source.details.externalData.withIntervals("42"),
            ),
            structure = null,
            startTime = time,
        )

        assertTrue(repository.isTrainerRoadWorkoutUpToDate(source, target))
        assertTrue(repository.isTrainerRoadWorkoutUpToDate(source.withStartTime(time), target))
        assertTrue(!repository.isTrainerRoadWorkoutUpToDate(source.withStartTime(LocalTime.NOON), target))
        assertTrue(!repository.isTrainerRoadWorkoutUpToDate(source.copy(details = source.details.copy(name = "Changed")), target))
        assertTrue(!repository.isTrainerRoadWorkoutUpToDate(source.copy(details = source.details.copy(type = TrainingType.RUN)), target))
        assertTrue(!repository.isTrainerRoadWorkoutUpToDate(source.copy(details = source.details.copy(description = "Changed")), target))
        val changedStep = source.copy(structure = WorkoutStructure(
            WorkoutStructure.TargetUnit.FTP_PERCENTAGE,
            listOf(SingleStep("Warmup", StepLength.seconds(301), StepTarget(50, 50), null, false)),
        ))
        assertTrue(!repository.isTrainerRoadWorkoutUpToDate(changedStep, target))
    }

    @Test
    fun `mirror retains TrainingPeaks ID when comparing updating and removing a marked event`() {
        val day = LocalDate.of(2026, 11, 23)
        val time = LocalTime.of(18, 30)
        val source = sourceForWrite(trainerRoadWorkout("Workout"))
        val sourceWithTrainingPeaks = source.copy(
            details = source.details.copy(externalData = source.details.externalData.withTrainingPeaks("456")),
        )
        val rendered = ToIntervalsWorkoutConverter().createEventRequestDTO(sourceWithTrainingPeaks.withStartTime(time))
        fun event(name: String, description: String) = objectMapper.readValue(
            """{"id":42,"name":${objectMapper.writeValueAsString(name)},"description":${objectMapper.writeValueAsString(description)},"start_date_local":"2026-11-23T18:30:00","category":"WORKOUT","type":"Ride"}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val api = RecordingClient(events = listOf(event("Workout", rendered.description)))
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val trainerRoad = mock(WorkoutRepository::class.java)
        `when`(trainerRoad.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(trainerRoad.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(source), emptyList()))
        val request = CopyFromCalendarToCalendarRequest(
            day, day, listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )
        val service = WorkoutService(listOf(trainerRoad, repository), emptyList())
        assertEquals(0, service.copyWorkoutsC2C(request).updated)
        assertTrue(api.updatedRequests.isEmpty())

        val changed = source.copy(details = source.details.copy(name = "Updated"))
        `when`(trainerRoad.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(changed), emptyList()))
        assertEquals(1, service.copyWorkoutsC2C(request).updated)
        val updated = api.updatedRequests.single().second
        val ids = ExternalData.empty().fromSimpleString(updated.description)
        assertEquals("456", ids.trainingPeaksId)
        assertEquals("123", ids.trainerRoadId)
        assertTrue(ids.hasClearTrainerRoadId(updated.description))

        val staleApi = RecordingClient(events = listOf(event(updated.name, updated.description)))
        val staleRepository = IntervalsWorkoutRepository(staleApi, intervalsConfigurationRepository)
        `when`(trainerRoad.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(emptyList(), emptyList()))
        val staleService = WorkoutService(listOf(trainerRoad, staleRepository), emptyList())
        assertEquals(1, staleService.copyWorkoutsC2C(request).removed)
        assertEquals(listOf("42"), staleApi.deletedEventIds)

        val ambiguousApi = RecordingClient(events = listOf(event(
            "Workout", "//////////\ntrainerRoadId=123\ntrainingPeaksId=456\ntrainingPeaksId=789",
        )))
        val ambiguousService = WorkoutService(
            listOf(trainerRoad, IntervalsWorkoutRepository(ambiguousApi, intervalsConfigurationRepository)), emptyList(),
        )
        val result = ambiguousService.copyWorkoutsC2C(request)
        assertEquals(0, result.removed)
        assertEquals(1, result.skipped)
        assertTrue(ambiguousApi.deletedEventIds.isEmpty())
    }

    @Test
    fun `mirror can remove a stale workout copied from TrainingPeaks with both IDs`() {
        val day = LocalDate.of(2026, 11, 23)
        val trainingPeaksWorkout = TPToWorkoutConverter().toWorkout(TPWorkoutCalendarResponseDTO(
            day.atStartOfDay(), "456", 2, "Workout", null, null,
            "Description\n//////////\ntrainerRoadId=123", null, null,
        ))
        val source = mock(WorkoutRepository::class.java)
        `when`(source.platform()).thenReturn(Platform.TRAINING_PEAKS)
        `when`(source.getWorkoutsFromCalendar(day, day)).thenReturn(listOf(trainingPeaksWorkout))
        val copyApi = RecordingClient()
        val copyService = WorkoutService(
            listOf(source, IntervalsWorkoutRepository(copyApi, intervalsConfigurationRepository)), emptyList(),
        )
        val copyRequest = CopyFromCalendarToCalendarRequest(
            day, day, listOf(TrainingType.BIKE), false, Platform.TRAINING_PEAKS, Platform.INTERVALS,
        )
        assertEquals(1, copyService.copyWorkoutsC2C(copyRequest).copied)
        val copied = copyApi.createdRequests.single()
        val ids = ExternalData.empty().fromSimpleString(copied.description)
        assertEquals("456", ids.trainingPeaksId)
        assertEquals("123", ids.trainerRoadId)

        val event = objectMapper.readValue(
            """{"id":42,"name":${objectMapper.writeValueAsString(copied.name)},"description":${objectMapper.writeValueAsString(copied.description)},"start_date_local":${objectMapper.writeValueAsString(copied.start_date_local)},"category":"WORKOUT","type":"Ride"}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val mirrorApi = RecordingClient(events = listOf(event))
        val trainerRoad = mock(WorkoutRepository::class.java)
        `when`(trainerRoad.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(trainerRoad.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(emptyList(), emptyList()))
        val mirrorService = WorkoutService(
            listOf(trainerRoad, IntervalsWorkoutRepository(mirrorApi, intervalsConfigurationRepository)), emptyList(),
        )
        val mirrorRequest = copyRequest.copy(
            sourcePlatform = Platform.TRAINER_ROAD, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true,
        )
        assertEquals(1, mirrorService.copyWorkoutsC2C(mirrorRequest).removed)
        assertEquals(listOf("42"), mirrorApi.deletedEventIds)
    }

    @Test
    fun `update requires matching IDs and a confirmed event response`() {
        val api = RecordingClient(confirmUpdate = false)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.updateSyncedWorkout(sourceForWrite(source).copy(date = source.date!!.plusDays(1)), target)
        }
        assertTrue(api.updatedRequests.isEmpty())
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.updateSyncedWorkout(sourceForWrite(source), target)
        }
        assertTrue(api.deletedEventIds.isEmpty())
    }

    @Test
    fun `operations require a single valid matching ID`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Workout")
        val target = source.copy(details = source.details.copy(externalData = source.details.externalData.withIntervals("42")))
        repository.updateSyncedWorkout(sourceForWrite(source), target)
        repository.deleteSyncedWorkout(target)
        assertEquals("42", api.updatedRequests.single().first)
        assertEquals(listOf("42"), api.deletedEventIds)

        for (description in listOf(
            "No metadata",
            "//////////\ntrainerRoadId=999",
            "//////////\ntrainerRoadId=123\ntrainerRoadId=999",
            "//////////\ntrainerRoadId=not-a-number",
            "//////////\ntrainerRoadId=123\ntrainingPeaksId=456\ntrainingPeaksId=789",
            "//////////\ntrainerRoadId=123\ntrainingPeaksId=",
        )) {
            val invalid = target.copy(details = target.details.copy(description = description))
            assertTrue(!repository.hasExplicitTrainerRoadId(invalid))
            assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
                repository.updateSyncedWorkout(sourceForWrite(source), invalid)
            }
            assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
                repository.deleteSyncedWorkout(invalid)
            }
        }
        assertEquals(1, api.updatedRequests.size)
        assertEquals(1, api.deletedEventIds.size)
    }

    @Test
    fun `mirror can change a marked event regardless of origin but protects an unmarked event`() {
        fun event(id: Int, description: String) = objectMapper.readValue(
            """{"id":$id,"name":"Manually edited","description":${objectMapper.writeValueAsString(description)},"start_date_local":"2026-11-23T18:00:00","category":"WORKOUT","type":"Ride"}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val api = RecordingClient(events = listOf(
            event(42, "Manually edited\n//////////\ntrainerRoadId=123"),
            event(43, "Manually created without a TrainerRoad ID"),
        ))
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = trainerRoadWorkout("Workout")
        val date = requireNotNull(source.date)
        val (marked, unmarked) = repository.getWorkoutsForReconciliation(date, date)

        repository.updateSyncedWorkout(sourceForWrite(source), marked)
        repository.deleteSyncedWorkout(marked)
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.updateSyncedWorkout(sourceForWrite(source), unmarked)
        }
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.deleteSyncedWorkout(unmarked)
        }
        assertEquals(listOf("42"), api.updatedRequests.map { it.first })
        assertEquals(listOf("42"), api.deletedEventIds)
    }

    @Test
    fun `missing create response fails`() {
        val api = RecordingClient(confirmCreate = false)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        assertThrows(org.freekode.tp2intervals.infrastructure.PlatformException::class.java) {
            repository.saveTrainerRoadWorkoutsToCalendar(listOf(sourceForWrite(trainerRoadWorkout("Workout"))))
        }
    }

    @Test
    fun `invalid marker in a batch fails before creating any events`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val valid = trainerRoadWorkout("Valid").let {
            it.copy(details = it.details.copy(description = "Warmup"))
        }
        val invalid = valid.copy(details = valid.details.copy(description = "//////////"))

        assertThrows(PlatformException::class.java) {
            repository.saveTrainerRoadWorkoutsToCalendar(listOf(valid, invalid))
        }
        assertTrue(api.createdRequests.isEmpty())
    }

    @Test
    fun `invalid marker in a later library batch fails before any workouts are created`() {
        val api = mock(IntervalsApiClient::class.java)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val valid = sourceForWrite(trainerRoadWorkout("Valid"))
        val invalid = valid.copy(details = valid.details.copy(description = "//////////"))
        val library = LibraryContainer("Library", requireNotNull(valid.date), false, 0, ExternalData.empty().withIntervals("1"))

        assertThrows(PlatformException::class.java) {
            repository.saveWorkoutsToLibrary(library, List(10) { valid } + invalid)
        }
        org.mockito.Mockito.verifyNoInteractions(api)
    }

    @Test
    fun `valid library workouts remain batched after prevalidation`() {
        val api = RecordingClient()
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val valid = sourceForWrite(trainerRoadWorkout("Valid"))
        val library = LibraryContainer("Library", requireNotNull(valid.date), false, 0, ExternalData.empty().withIntervals("1"))

        repository.saveWorkoutsToLibrary(library, List(11) { valid })

        assertEquals(listOf(10, 1), api.workoutBatches.map { it.size })
        assertTrue(api.workoutBatches.flatten().all { valid.details.externalData.hasClearTrainerRoadId(it.description) })
    }

    @Test
    fun `reconciliation uses description identity without parsing workout structures`() {
        val api = mock(IntervalsApiClient::class.java)
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val event = objectMapper.readValue(
            """{"id":42,"name":"Edited","description":"No sync metadata","start_date_local":"2026-11-23T18:00:00","category":"WORKOUT","type":"Ride","workout_doc":{"target":"UNKNOWN","steps":[{}]}}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val unrelated = objectMapper.readValue(
            """{"id":43,"name":"Other","description":"//////////\ntrainerRoadId","start_date_local":"2026-11-23T19:00:00","category":"WORKOUT","type":"Ride","workout_doc":{"target":"UNKNOWN","steps":[{}]}}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val date = LocalDate.of(2026, 11, 23)
        `when`(api.getEventsForReconciliation("athleteId", date.toString(), date.toString()))
            .thenReturn(listOf(event, unrelated))

        val results = repository.getWorkoutsForReconciliation(date, date)

        assertEquals(2, results.size)
        assertEquals(null, results[0].details.externalData.trainerRoadId)
        assertEquals(null, results[0].structure)
        assertEquals(null, results[1].details.externalData.trainerRoadId)
    }

    @Test
    fun `ambiguous metadata blocks copies and stale deletions for every candidate ID`() {
        val ambiguousDescription = "//////////\ntrainerRoadId=1\n//////////\ntrainerRoadId=2"
        fun event(id: Int, description: String) = objectMapper.readValue(
            """{"id":$id,"name":"Workout","description":${objectMapper.writeValueAsString(description)},"start_date_local":"2026-11-23T18:00:00","category":"WORKOUT","type":"Ride"}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val api = RecordingClient(events = listOf(
            event(42, ambiguousDescription),
            event(43, "//////////\ntrainerRoadId=1"),
        ))
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.INTERVALS, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true)

        for (id in listOf("1", "2")) {
            val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout $id", null, null, null,
                ExternalData.empty().withTrainerRoad(id)), day, null)
            `when`(source.getTrainerRoadReconciliationSource(day, day))
                .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
            val result = service.copyWorkoutsC2C(request)
            assertEquals(0, result.copied)
            assertEquals(0, result.removed)
            assertTrue(result.skipped > 0)
            if (id == "1") {
                assertEquals(setOf("42", "43"), result.skippedWorkouts.single().intervalsIds.toSet())
            }
            assertEquals(0, service.copyWorkoutsC2C(request).copied)
        }
        assertTrue(api.createdRequests.isEmpty())
        assertTrue(api.updatedRequests.isEmpty())
        assertTrue(api.deletedEventIds.isEmpty())
        assertEquals(listOf(42L, 43L), api.getEventsForReconciliation("athleteId", day.toString(), day.toString()).map { it.id })
    }

    @Test
    fun `ambiguous marker on another date does not block a copy`() {
        val event = objectMapper.readValue(
            """{"id":42,"name":"Workout","description":"//////////\ntrainerRoadId=1\ntrainerRoadId=2","start_date_local":"2026-11-22T18:00:00","category":"WORKOUT","type":"Ride"}""",
            IntervalsEventIdentityDTO::class.java,
        )
        val api = RecordingClient(events = listOf(event))
        val repository = IntervalsWorkoutRepository(api, intervalsConfigurationRepository)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null,
            ExternalData.empty().withTrainerRoad("2")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.INTERVALS, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        assertEquals(1, service.copyWorkoutsC2C(request).copied)
        assertEquals(1, api.createdRequests.size)
        assertTrue(api.deletedEventIds.isEmpty())
    }

    private class RecordingClient(
        private val failDelete: Boolean = false,
        private val confirmUpdate: Boolean = true,
        private val confirmCreate: Boolean = true,
        private val events: List<IntervalsEventIdentityDTO> = emptyList(),
    ) :
        IntervalsApiClient by mock(IntervalsApiClient::class.java) {
        val deletedEventIds = mutableListOf<String>()
        val updatedRequests = mutableListOf<Pair<String, CreateEventRequestDTO>>()
        val createdRequests = mutableListOf<CreateEventRequestDTO>()
        val workoutBatches = mutableListOf<List<CreateWorkoutRequestDTO>>()

        override fun getEventsForReconciliation(athleteId: String, startDate: String, endDate: String) = events

        override fun createWorkouts(athleteId: String, requests: List<CreateWorkoutRequestDTO>) {
            workoutBatches.add(requests)
        }

        override fun createEvent(athleteId: String, createEventRequestDTO: CreateEventRequestDTO): EventWriteResponseDTO? {
            createdRequests.add(createEventRequestDTO)
            return if (confirmCreate) EventWriteResponseDTO(42) else null
        }

        override fun deleteEvent(athleteId: String, eventId: String) {
            if (failDelete) throw IllegalStateException("delete failed")
            deletedEventIds.add(eventId)
        }

        override fun updateEvent(athleteId: String, eventId: String, request: CreateEventRequestDTO): EventWriteResponseDTO? {
            updatedRequests.add(eventId to request)
            return if (confirmUpdate) EventWriteResponseDTO(eventId.toLong()) else null
        }
    }

    private fun trainerRoadWorkout(name: String) = Workout(
        WorkoutDetails(TrainingType.BIKE, name, ExternalData.empty().withTrainerRoad("123").toSimpleString(), null, null, ExternalData.empty().withTrainerRoad("123")),
        LocalDate.of(2026, 11, 23),
        null,
    )

    private fun sourceForWrite(workout: Workout) =
        workout.copy(details = workout.details.copy(description = "Workout description"))

    private fun findWorkoutWithName(name: String, workouts: List<Workout>): Workout {
        return workouts.find { it.details.name == name }!!
    }

    private fun getIntervalsConfigurationRepository(): IntervalsConfigurationRepository {
        val repo = mock(IntervalsConfigurationRepository::class.java)
        `when`(repo.getConfiguration()).thenReturn(IntervalsConfiguration("apiKey", "athleteId", 0.1f, 0.2f, 0.3f))
        return repo
    }
}
