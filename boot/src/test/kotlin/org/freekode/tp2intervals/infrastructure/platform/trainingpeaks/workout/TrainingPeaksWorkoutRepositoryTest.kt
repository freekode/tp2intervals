package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import config.TestUtils
import config.mock.ObjectMapperFactory
import config.mock.TrainingPeaksApiClientMock
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.domain.workout.TrainerRoadReconciliationSource
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.TrainerRoadSyncMode
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.MultiStep
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksReconciliationApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanCoachApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUser
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import feign.FeignException
import feign.Request
import feign.Response
import org.freekode.tp2intervals.infrastructure.CatchFeignExceptionAspect
import org.freekode.tp2intervals.rest.RestResponseEntityExceptionHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.util.ResourceUtils
import org.springframework.web.context.request.ServletWebRequest
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class TrainingPeaksWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val trainingPeaksUserRepository = trainingPeaksUserRepository()

    @Test
    fun `ordinary calendar sync translates TrainingPeaks write failures through the proxy`() {
        var status = 402
        val api = mock(TrainingPeaksApiClient::class.java) { invocation ->
            if (invocation.method.name == "createAndPlanWorkout") {
                val request = Request.create(
                    Request.HttpMethod.POST, "https://example.invalid/synthetic-account",
                    emptyMap(), null, StandardCharsets.UTF_8, null,
                )
                val response = Response.builder().status(status).reason("failure").request(request)
                    .body("synthetic response body", StandardCharsets.UTF_8).build()
                throw FeignException.errorStatus("createAndPlanWorkout", response)
            }
            org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation)
        }
        val factory = AspectJProxyFactory(trainingPeaksWorkoutRepository(api))
        factory.setProxyTargetClass(true)
        factory.addAspect(CatchFeignExceptionAspect())
        val target = factory.getProxy<WorkoutRepository>()
        val source = mock(WorkoutRepository::class.java)
        val date = LocalDate.of(2026, 11, 26)
        val workout = Workout(
            WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null, ExternalData.empty()),
            date, null,
        )
        `when`(source.platform()).thenReturn(Platform.INTERVALS)
        `when`(source.getWorkoutsFromCalendar(date, date)).thenReturn(listOf(workout))
        val service = WorkoutService(listOf(source, target), emptyList())
        val request = CopyFromCalendarToCalendarRequest(
            date, date, listOf(TrainingType.BIKE), false, Platform.INTERVALS, Platform.TRAINING_PEAKS,
        )

        val error = assertThrows(PlatformException::class.java) { service.copyWorkoutsC2C(request) }
        val response = RestResponseEntityExceptionHandler()
            .platformException(error, ServletWebRequest(MockHttpServletRequest()))
        assertEquals(Platform.TRAINING_PEAKS.title, response.body!!.platform)
        assertEquals(
            "HTTP 402: Free accounts have limits on syncing future workouts. " +
                "Tomorrow may be available later; try again then or sync today. " +
                "Check your calendar before retrying, as some workouts may already have been synced.",
            response.body!!.message,
        )
        status = 401
        assertEquals("Access denied", assertThrows(PlatformException::class.java) { service.copyWorkoutsC2C(request) }.message)
        status = 500
        assertEquals("HTTP request failed with status 500", assertThrows(PlatformException::class.java) { service.copyWorkoutsC2C(request) }.message)
    }

    @Test
    fun `should parse swim workout with distance based steps`() {
        // when
        val trainingPeaksApiClient = TrainingPeaksApiClientMock(
            objectMapper,
            ResourceUtils.getFile("classpath:tp-calendar-workout-distant-steps.json").inputStream()
        )
        val trainingPeaksWorkoutRepository = trainingPeaksWorkoutRepository(trainingPeaksApiClient)
        val workouts =
            trainingPeaksWorkoutRepository.getWorkoutsFromCalendar(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1))

        // then
        assertTrue(workouts.isNotEmpty())

        val workout = workouts[0]
        val structure = workout.structure!!

        assertEquals(TrainingType.SWIM, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, structure.target)
        assertEquals(8, structure.steps.size)

        val multiStep1 = (structure.steps[0] as MultiStep)
        assertEquals(3, multiStep1.steps.size)
        TestUtils.assertStep(multiStep1.steps[0], 50, StepLength.LengthUnit.METERS, 75, 75)

        TestUtils.assertStep(structure.steps[1] as SingleStep, 10, StepLength.LengthUnit.SECONDS, 0, 0)
        TestUtils.assertStep(structure.steps[3] as SingleStep, 200, StepLength.LengthUnit.METERS, 75, 75)

        val multiStep2 = (structure.steps[4] as MultiStep)
        assertEquals(2, multiStep2.steps.size)
        TestUtils.assertStep(multiStep2.steps[0], 50, StepLength.LengthUnit.METERS, 90, 90)
        TestUtils.assertStep(multiStep2.steps[1], 10, StepLength.LengthUnit.SECONDS, 0, 0)
    }

    @Test
    fun `should parse workout with null title`() {
        // when
        val trainingPeaksApiClient = TrainingPeaksApiClientMock(
            objectMapper,
            ResourceUtils.getFile("classpath:tp-calendar-workout-null-title.json").inputStream()
        )
        val trainingPeaksWorkoutRepository = trainingPeaksWorkoutRepository(trainingPeaksApiClient)
        val workouts =
            trainingPeaksWorkoutRepository.getWorkoutsFromCalendar(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1))

        // then
        assertTrue(workouts.isNotEmpty())

        val workout = workouts[0]
        val structure = workout.structure!!

        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(10, structure.steps.size)
    }

    @Test
    fun `finds library workout when only one external id is supplied`() {
        val library = mock(TPWorkoutLibraryRepository::class.java)
        val workout = Workout(
            WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null,
                ExternalData.empty().withTrainingPeaks("123").withTrainerRoad("456")),
            LocalDate.of(2026, 11, 23), null,
        )
        `when`(library.getAllWorkouts()).thenReturn(listOf(workout))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), library)

        assertEquals(workout, repository.getWorkoutFromLibrary(ExternalData.empty().withTrainingPeaks("123")))
    }

    @Test
    fun `library lookup prioritizes the selected TrainingPeaks ID over shared source IDs`() {
        val library = mock(TPWorkoutLibraryRepository::class.java)
        val first = Workout(
            WorkoutDetails(TrainingType.BIKE, "First", null, null, null,
                ExternalData.empty().withTrainingPeaks("1").withTrainerRoad("456")),
            null, null,
        )
        val selected = first.copy(details = first.details.copy(
            name = "Selected", externalData = ExternalData.empty().withTrainingPeaks("2").withTrainerRoad("456"),
        ))
        `when`(library.getAllWorkouts()).thenReturn(listOf(first, selected))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), library)

        assertEquals(selected, repository.getWorkoutFromLibrary(selected.details.externalData))
    }

    @Test
    fun `reconciliation protects completed and incomplete status workouts`() {
        val api = mock(TrainingPeaksReconciliationApiClient::class.java)
        val planned = reconciliationWorkout()
        val completed = reconciliationWorkout().put("completed", true)
        val actual = reconciliationWorkout().put("totalTime", 1.0)
        val actualStart = reconciliationWorkout().put("startTime", "2026-11-23T09:00:00")
        val unknown = reconciliationWorkout().apply { remove("completed") }
        val unknownTime = reconciliationWorkout().apply { remove("startTimePlanned") }
        val invalidTime = reconciliationWorkout().put("startTimePlanned", "invalid")
        val wrongDay = reconciliationWorkout().put("startTimePlanned", "2026-11-24T07:30:00")
        `when`(api.getWorkouts("123", "2026-11-23", "2026-11-23"))
            .thenReturn(listOf(planned, completed, actual, actualStart, unknown, unknownTime, invalidTime, wrongDay))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)

        val workouts = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23))

        assertTrue(repository.hasExplicitTrainerRoadId(workouts[0]))
        workouts.drop(1).forEach { assertFalse(repository.hasExplicitTrainerRoadId(it)) }
    }

    @Test
    fun `rechecks completion before deleting a marked workout`() {
        val api = mock(TrainingPeaksReconciliationApiClient::class.java)
        val planned = reconciliationWorkout()
        val completed = reconciliationWorkout().put("completed", true)
        `when`(api.getWorkouts("123", "2026-11-23", "2026-11-23")).thenReturn(listOf(planned))
        `when`(api.getWorkout("123", "10")).thenReturn(completed)
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val workout = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()

        assertThrows(PlatformException::class.java) { repository.deleteSyncedWorkout(workout) }
        verify(api, never()).deleteWorkout("123", "10")
    }

    @Test
    fun `rechecks completion before updating a marked workout`() {
        val api = mock(TrainingPeaksReconciliationApiClient::class.java)
        `when`(api.getWorkouts("123", "2026-11-23", "2026-11-23")).thenReturn(listOf(reconciliationWorkout()))
        `when`(api.getWorkout("123", "10")).thenReturn(reconciliationWorkout().put("completed", true))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val existing = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        val source = existing.copy(details = existing.details.copy(name = "Changed", description = null))

        assertThrows(PlatformException::class.java) { repository.updateSyncedWorkout(source, existing) }
        verify(api).getWorkouts("123", "2026-11-23", "2026-11-23")
        verify(api).getWorkout("123", "10")
        verifyNoMoreInteractions(api)
    }

    @Test
    fun `additional actual metrics protect workouts and are rechecked before writes`() {
        val api = FakeReconciliationClient(reconciliationWorkout())
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val day = LocalDate.of(2026, 11, 23)
        val existing = repository.getWorkoutsForReconciliation(day, day).single()
        val source = existing.copy(details = existing.details.copy(name = "Changed", description = null))
        val original = api.workout.deepCopy()
        val sourceRepository = mock(WorkoutRepository::class.java)
        `when`(sourceRepository.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(sourceRepository.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(source), emptyList()))
        val service = WorkoutService(listOf(sourceRepository, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true)

        for (field in listOf("heartRateAverage", "normalizedPowerActual", "cadenceMaximum", "rpe")) {
            api.workout.put(field, 140)
            assertFalse(repository.hasExplicitTrainerRoadId(repository.getWorkoutsForReconciliation(day, day).single()))
            val result = service.copyWorkoutsC2C(request)
            assertEquals(1, result.skipped)
            assertEquals(0, result.updated)
            assertEquals(0, result.removed)
            assertThrows(PlatformException::class.java) { repository.updateSyncedWorkout(source, existing) }
            assertThrows(PlatformException::class.java) { repository.deleteSyncedWorkout(existing) }
            api.workout.remove(field)
        }
        assertEquals(0, api.updates)
        assertEquals(0, api.deletes)
        assertEquals(original, api.workout)
        assertTrue(repository.hasExplicitTrainerRoadId(repository.getWorkoutsForReconciliation(day, day).single()))
    }

    @Test
    fun `updates marked planned workouts without replacing unrelated fields`() {
        val api = FakeReconciliationClient(reconciliationWorkout().put("coachComments", "Keep this"))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val existing = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        val source = Workout(
            WorkoutDetails(TrainingType.VIRTUAL_BIKE, "New name", null, null, null, ExternalData.empty().withTrainerRoad("1")),
            existing.date, null,
        )

        repository.updateSyncedWorkout(source, existing)

        assertEquals("New name", api.workout.path("title").asText())
        assertEquals("Keep this", api.workout.path("coachComments").asText())
        val updated = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        assertTrue(repository.isTrainerRoadWorkoutUpToDate(source, updated))
    }

    @Test
    fun `mirror writes planned time and considers the read back time unchanged`() {
        val api = FakeReconciliationClient(reconciliationWorkout().put("startTimePlanned", "2026-11-23T09:00:00"))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val existing = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        val source = existing.copy(details = existing.details.copy(description = null, externalData = ExternalData.empty().withTrainerRoad("1")),
            startTime = LocalTime.of(7, 30))
        assertTrue(repository.hasExplicitTrainerRoadId(existing))
        assertFalse(repository.isTrainerRoadWorkoutUpToDate(source, existing))

        repository.updateSyncedWorkout(source, existing)
        assertEquals("2026-11-23T07:30:00", api.workout.path("startTimePlanned").asText())
        assertTrue(api.workout.path("startTime").isNull)
        val updated = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        assertTrue(repository.isTrainerRoadWorkoutUpToDate(source, updated))
    }

    @Test
    fun `mirror preserves freshly read planned time when source has no time`() {
        val api = FakeReconciliationClient(reconciliationWorkout().put("startTimePlanned", "2026-11-23T09:00:00"))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val existing = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        val source = existing.copy(details = existing.details.copy(name = "Changed", description = null), startTime = null)
        api.workout.put("startTimePlanned", "2026-11-23T19:45:00")

        repository.updateSyncedWorkout(source, existing)

        assertEquals("2026-11-23T19:45:00", api.workout.path("startTimePlanned").asText())
        val updated = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        assertTrue(repository.isTrainerRoadWorkoutUpToDate(source, updated))
    }

    @Test
    fun `new workouts use TrainerRoad time or configured fallback`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workouts = listOf("1", "2").map { id ->
            Workout(WorkoutDetails(TrainingType.BIKE, "Workout $id", null, null, null,
                ExternalData.empty().withTrainerRoad(id)), day, null)
        }.let { listOf(it[0].withStartTime(LocalTime.of(7, 30)), it[1]) }
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(workouts, emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, defaultStartTime = "18:00",
            trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        assertEquals(2, service.copyWorkoutsC2C(request).copied)
        assertEquals(listOf("2026-11-23T07:30:00", "2026-11-23T18:00:00"),
            api.workouts.map { it.path("startTimePlanned").asText() })
        assertEquals(0, service.copyWorkoutsC2C(request).copied)
        assertEquals(2, api.creates)
    }

    @Test
    fun `mirror applies fallback once and leaves the time unchanged on replay`() {
        val api = FakeReconciliationClient(reconciliationWorkout().put("startTimePlanned", "2026-11-23T09:00:00"))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, defaultStartTime = "18:00",
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)

        assertEquals(1, service.copyWorkoutsC2C(request).updated)
        assertEquals("2026-11-23T18:00:00", api.workout.path("startTimePlanned").asText())
        val replay = service.copyWorkoutsC2C(request)
        assertEquals(0, replay.updated)
        assertEquals(1, replay.alreadySynced)
        assertEquals(1, api.updates)
    }

    @Test
    fun `structured workout does not need another update after it is read back`() {
        val api = FakeReconciliationClient(reconciliationWorkout())
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val existing = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        val structure = WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE,
            listOf(SingleStep("Interval", StepLength.seconds(300), StepTarget(80, 90), null, false)))
        val source = Workout(existing.details.copy(name = "Intervals", description = null,
            externalData = ExternalData.empty().withTrainerRoad("1")), existing.date, structure)

        repository.updateSyncedWorkout(source, existing)
        val updated = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()

        assertTrue(repository.isTrainerRoadWorkoutUpToDate(source, updated))
    }

    @Test
    fun `new TrainerRoad workouts include the description and remain identifiable`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", "Ride steadily", null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        assertEquals(1, service.copyWorkoutsC2C(request).copied)
        assertEquals("Ride steadily\n\n//////////\ntrainerRoadId=1", api.workouts.single().path("description").asText())
        assertEquals(0, service.copyWorkoutsC2C(request).copied)
        assertEquals(1, api.creates)
    }

    @Test
    fun `missing structure length or intensity protects a marked workout`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val day = LocalDate.of(2026, 11, 23)
        val structures = listOf(
            """{"primaryIntensityMetric":"percentOfFtp","structure":[{"type":"step","steps":[{"targets":[{"minValue":50}]}]}]}""",
            """{"structure":[{"type":"step","steps":[{"length":{"value":300,"unit":"second"},"targets":[{"minValue":50}]}]}]}""",
        )
        for (structure in structures) {
            val workout = reconciliationWorkout()
            workout.set<JsonNode>("structure", objectMapper.readTree(structure))
            api.workouts.clear()
            api.workouts.add(workout)
            val result = repository.getWorkoutsForReconciliation(day, day).single()
            assertEquals(null, result.structure)
            assertFalse(repository.hasExplicitTrainerRoadId(result))
        }
    }

    @Test
    fun `mirror copies changed TrainerRoad descriptions and does not repeat unchanged updates`() {
        val api = FakeReconciliationClient(reconciliationWorkout())
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", "First description", null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true)

        assertEquals(1, service.copyWorkoutsC2C(request).updated)
        assertEquals("First description\n\n//////////\ntrainerRoadId=1", api.workout.path("description").asText())
        assertEquals(0, service.copyWorkoutsC2C(request).updated)
        val changed = workout.copy(details = workout.details.copy(description = "Changed description"))
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(changed), emptyList()))
        assertEquals(1, service.copyWorkoutsC2C(request).updated)
        assertEquals("Changed description\n\n//////////\ntrainerRoadId=1", api.workout.path("description").asText())
        assertEquals(2, api.updates)
    }

    @Test
    fun `rejects conflicting ID markers before creating a batch`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", "Normal description", null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        val conflicting = workout.copy(details = workout.details.copy(
            description = "Text containing //////////", externalData = ExternalData.empty().withTrainerRoad("2")))

        assertThrows(PlatformException::class.java) {
            repository.saveTrainerRoadWorkoutsToCalendar(listOf(workout, conflicting))
        }
        assertEquals(0, api.creates)
        assertTrue(api.workouts.isEmpty())
    }

    @Test
    fun `mirror does not rewrite a 123 minute workout due to floating point rounding`() {
        val api = FakeReconciliationClient(reconciliationWorkout().put("totalTimePlanned", 2.05))
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", null, Duration.ofMinutes(123), null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true)

        assertEquals(1, service.copyWorkoutsC2C(request).alreadySynced)
        assertEquals(1, service.copyWorkoutsC2C(request).alreadySynced)
        assertEquals(0, api.updates)
        api.workout.put("totalTimePlanned", 2.058333333333333)
        assertEquals(Duration.ofSeconds(7410), repository.getWorkoutsForReconciliation(day, day).single().details.duration)
    }

    @Test
    fun `single value cadence in an unrelated workout does not block sync`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val unrelated = reconciliationWorkout().put("description", "Unrelated workout")
        unrelated.set<JsonNode>("structure", objectMapper.readTree(
            """{"primaryLengthMetric":"duration","primaryIntensityMetric":"percentOfFtp","structure":[
                {"type":"step","steps":[{"name":"First","length":{"value":300,"unit":"second"},
                    "targets":[{"minValue":70},{"unit":"roundOrStridePerMinute","minValue":90}]}]},
                {"type":"step","steps":[{"name":"Second","length":{"value":300,"unit":"second"},
                    "targets":[{"minValue":70},{"unit":"roundOrStridePerMinute","maxValue":95}]}]}
            ]}"""
        ))
        api.workouts.add(unrelated)
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "TrainerRoad workout", null, null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        val steps = repository.getWorkoutsForReconciliation(day, day).single().structure!!.steps
        assertEquals(StepTarget(90, 90), (steps[0] as SingleStep).cadence)
        assertEquals(StepTarget(95, 95), (steps[1] as SingleStep).cadence)
        assertEquals(1, service.copyWorkoutsC2C(request).copied)
        assertEquals(0, service.copyWorkoutsC2C(request).copied)
        assertEquals(2, api.workouts.size)
        assertEquals("Unrelated workout", api.workouts[0].path("description").asText())
        assertEquals(1, api.creates)
    }

    @Test
    fun `unreadable structures do not block unrelated copies and protect matching workouts`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val unrelated = reconciliationWorkout().put("description", "Unrelated workout")
        val structure = objectMapper.readTree(
            """{"primaryLengthMetric":"duration","primaryIntensityMetric":"percentOfFtp","structure":[
                {"type":"step","steps":[{"name":"Free ride","length":{"value":300,"unit":"second"},"targets":[]}]}
            ]}"""
        )
        unrelated.set<JsonNode>("structure", structure)
        api.workouts.add(unrelated)
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "TrainerRoad workout", null, null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true)

        val unrelatedRead = repository.getWorkoutsForReconciliation(day, day).single()
        assertEquals(null, unrelatedRead.structure)
        assertEquals(1, service.copyWorkoutsC2C(request).copied)
        assertEquals("Unrelated workout", api.workouts.first().path("description").asText())

        api.workouts.removeAt(1)
        api.workouts.first().put("description", "//////////\ntrainerRoadId=1")
        val result = service.copyWorkoutsC2C(request)
        assertEquals(1, result.skipped)
        assertEquals(0, result.updated)
        assertEquals(0, result.removed)
        assertEquals(1, api.workouts.size)
        assertEquals(1, api.creates)
    }

    @Test
    fun `ambiguous TrainingPeaks markers never create duplicates or delete existing workouts`() {
        for (description in listOf(
            "//////////\ntrainerRoadId=1\ntrainerRoadId=2",
            "//////////\ntrainerRoadId=1\n//////////\ntrainerRoadId=2",
            "//////////\ntrainerRoadId=2\ntrainerRoadId=2",
        )) {
            val api = MutableCalendarClient().apply { loseFirstResponse = false }
            api.workouts.add(reconciliationWorkout().put("description", description))
            val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
            val source = mock(WorkoutRepository::class.java)
            val day = LocalDate.of(2026, 11, 23)
            val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout 2", null, null, null,
                ExternalData.empty().withTrainerRoad("2")), day, null)
            `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
            `when`(source.getTrainerRoadReconciliationSource(day, day))
                .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
            val service = WorkoutService(listOf(source, repository), emptyList())
            val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
                Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

            assertEquals(1, service.copyWorkoutsC2C(request).skipped)
            val mirror = request.copy(trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)
            assertEquals(0, service.copyWorkoutsC2C(mirror).copied)
            assertEquals(0, service.copyWorkoutsC2C(mirror).removed)
            assertEquals(listOf(description), api.workouts.map { it.path("description").asText() })
            assertEquals(0, api.creates)
        }
    }

    @Test
    fun `deletes only a still planned workout with the same marked identity`() {
        val api = FakeReconciliationClient(reconciliationWorkout())
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val workout = repository.getWorkoutsForReconciliation(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23)).single()
        api.workout.put("description", "//////////\ntrainerRoadId=2")

        assertThrows(PlatformException::class.java) { repository.deleteSyncedWorkout(workout) }
        assertEquals(0, api.deletes)
        api.workout.put("description", "//////////\ntrainerRoadId=1")
        repository.deleteSyncedWorkout(workout)
        assertEquals(1, api.deletes)
    }

    @Test
    fun `add missing reconciles a committed write with a lost response before retry`() {
        val api = MutableCalendarClient()
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workouts = listOf("1", "2").map { id ->
            Workout(WorkoutDetails(TrainingType.BIKE, "Workout $id", null, null, null,
                ExternalData.empty().withTrainerRoad(id)), day, null)
        }
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(workouts, emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        assertThrows(IllegalStateException::class.java) { service.copyWorkoutsC2C(request) }
        assertEquals(1, api.workouts.size)
        assertEquals(1, service.copyWorkoutsC2C(request).copied)
        assertEquals(0, service.copyWorkoutsC2C(request).copied)
        assertEquals(2, api.workouts.size)
        assertEquals(2, api.creates)
        assertEquals(setOf("//////////\ntrainerRoadId=1", "//////////\ntrainerRoadId=2"),
            api.workouts.map { it.path("description").asText() }.toSet())
    }

    @Test
    fun `overlapping manual and scheduled syncs create one workout`() {
        val api = MutableCalendarClient().apply { loseFirstResponse = false }
        val enteredCreate = CountDownLatch(1)
        val releaseCreate = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)
        api.beforeCreate = {
            enteredCreate.countDown()
            check(releaseCreate.await(5, TimeUnit.SECONDS))
        }
        val repository = trainingPeaksWorkoutRepository(mock(TrainingPeaksApiClient::class.java), reconciliation = api)
        val source = mock(WorkoutRepository::class.java)
        val day = LocalDate.of(2026, 11, 23)
        val workout = Workout(WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null,
            ExternalData.empty().withTrainerRoad("1")), day, null)
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(source.getTrainerRoadReconciliationSource(day, day))
            .thenReturn(TrainerRoadReconciliationSource(listOf(workout), emptyList()))
        val service = WorkoutService(listOf(source, repository), emptyList())
        val request = CopyFromCalendarToCalendarRequest(day, day, listOf(TrainingType.BIKE), true,
            Platform.TRAINER_ROAD, Platform.TRAINING_PEAKS, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val manual = executor.submit<Int> { service.copyWorkoutsC2C(request).copied }
            assertTrue(enteredCreate.await(5, TimeUnit.SECONDS))
            val scheduled = executor.submit<Int> {
                secondStarted.countDown()
                service.copyWorkoutsC2C(request).copied
            }
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS))
            releaseCreate.countDown()
            assertEquals(1, manual.get(5, TimeUnit.SECONDS))
            assertEquals(0, scheduled.get(5, TimeUnit.SECONDS))
            assertEquals(1, api.creates)
            assertEquals(1, api.workouts.size)
        } finally {
            releaseCreate.countDown()
            executor.shutdownNow()
        }
    }

    private inner class MutableCalendarClient : TrainingPeaksReconciliationApiClient {
        val workouts = mutableListOf<ObjectNode>()
        var creates = 0
        var loseFirstResponse = true
        var beforeCreate: () -> Unit = {}
        override fun getWorkouts(userId: String, startDate: String, endDate: String) = workouts.map { it.deepCopy() }
        override fun getWorkout(userId: String, workoutId: String) =
            workouts.single { it.path("workoutId").asText() == workoutId }.deepCopy()
        override fun createWorkout(userId: String, request: CreateTPWorkoutRequestDTO): JsonNode {
            beforeCreate()
            creates++
            val saved = objectMapper.valueToTree<ObjectNode>(request)
            saved.put("workoutId", creates + 10)
            saved.put("workoutDay", request.workoutDay.atStartOfDay().toString())
            if (!saved.has("startTimePlanned")) saved.putNull("startTimePlanned")
            listOf("completed", "totalTime", "startTime", "distance", "tssActual", "calories", "powerAverage")
                .forEach { saved.putNull(it) }
            workouts.add(saved)
            if (loseFirstResponse) {
                loseFirstResponse = false
                throw IllegalStateException("response lost")
            }
            return saved.deepCopy()
        }
        override fun updateWorkout(userId: String, workoutId: String, request: ObjectNode): JsonNode =
            throw UnsupportedOperationException()
        override fun deleteWorkout(userId: String, workoutId: String): Boolean =
            throw UnsupportedOperationException()
    }

    private inner class FakeReconciliationClient(var workout: ObjectNode) : TrainingPeaksReconciliationApiClient {
        var deletes = 0
        var updates = 0
        override fun getWorkouts(userId: String, startDate: String, endDate: String) = listOf(workout.deepCopy())
        override fun getWorkout(userId: String, workoutId: String) = workout.deepCopy()
        override fun createWorkout(userId: String, request: CreateTPWorkoutRequestDTO): JsonNode =
            throw UnsupportedOperationException()
        override fun updateWorkout(userId: String, workoutId: String, request: ObjectNode): JsonNode {
            updates++
            workout = request.deepCopy()
            if (workout.path("structure").isTextual) {
                workout.set<JsonNode>("structure", objectMapper.readTree(workout.path("structure").asText()))
            }
            return workout.deepCopy()
        }
        override fun deleteWorkout(userId: String, workoutId: String): Boolean {
            deletes++
            return true
        }
    }

    private fun reconciliationWorkout() = objectMapper.readTree(
        """{"workoutId":10,"workoutDay":"2026-11-23T00:00:00","workoutTypeValueId":2,
            "title":"Workout","description":"//////////\ntrainerRoadId=1",
            "completed":null,"totalTime":null,"startTime":null,"startTimePlanned":null,"distance":null,
            "tssActual":null,"calories":null,"powerAverage":null}""",
    ) as com.fasterxml.jackson.databind.node.ObjectNode

    private fun trainingPeaksUserRepository(): TrainingPeaksUserRepository {
        val mock = mock(TrainingPeaksUserRepository::class.java)
        `when`(mock.getUser()).thenReturn(TrainingPeaksUser("123", true, false))
        return mock
    }

    private fun trainingPeaksWorkoutRepository(
        trainingPeaksApiClient: TrainingPeaksApiClient,
        library: TPWorkoutLibraryRepository = mock(TPWorkoutLibraryRepository::class.java),
        reconciliation: TrainingPeaksReconciliationApiClient = mock(TrainingPeaksReconciliationApiClient::class.java),
    ) =
        TrainingPeaksWorkoutRepository(
            trainingPeaksApiClient,
            mock(TrainingPeaksPlanCoachApiClient::class.java),
            TPToWorkoutConverter(),
            mock(TrainingPeaksPlanRepository::class.java),
            trainingPeaksUserRepository,
            library,
            mock(TPAttachmentService::class.java),
            mock(TrainingPeaksConfigurationRepository::class.java),
            objectMapper,
            reconciliation,
        )
}
