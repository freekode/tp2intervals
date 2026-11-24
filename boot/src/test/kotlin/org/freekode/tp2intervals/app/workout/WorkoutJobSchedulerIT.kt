package org.freekode.tp2intervals.app.workout

import config.BaseSpringITConfig
import org.assertj.core.api.Assertions.assertThat
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.app.workout.schedule.ScheduledSyncResult
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import config.mock.ObjectMapperFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate

class WorkoutJobSchedulerIT : BaseSpringITConfig() {
    @Autowired
    lateinit var workoutScheduledJob: WorkoutScheduledJob

    @Autowired
    lateinit var scheduleRequestRepository: ScheduleRequestRepository

    @Test
    fun `scheduled result is stored without recreating a deleted job`() {
        val saved = scheduleRequestRepository.save(ScheduleRequestEntity("""{"test":true}"""))
        val id = requireNotNull(saved.id)
        assertThat(scheduleRequestRepository.updateLastResult(id, """{"failed":false}""")).isEqualTo(1)
        assertThat(scheduleRequestRepository.findById(id).orElseThrow().lastResultJson).isEqualTo("""{"failed":false}""")
        scheduleRequestRepository.deleteById(id)
        assertThat(scheduleRequestRepository.updateLastResult(id, """{"failed":true}""")).isEqualTo(0)
        assertThat(scheduleRequestRepository.findById(id)).isEmpty
    }

    @Test
    fun test() {
        val request =
            C2CTodayScheduledRequest(listOf(TrainingType.BIKE), true, Platform.INTERVALS, Platform.TRAINING_PEAKS)
        workoutScheduledJob.addRequest(request)

        val requests = workoutScheduledJob.getRequests()

        assertThat(requests.isNotEmpty()).isTrue()
        assertThat(requests[0] is ScheduleRequestEntity).isTrue()
    }

    @Test
    fun `mirror schedule without consent is rejected before saving`() {
        val before = workoutScheduledJob.getRequests().size
        val request = C2CTodayScheduledRequest(
            listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, mirrorByTrainerRoadId = true, allowUnidentifiedSourceRemoval = true,
        )

        assertThrows(PlatformException::class.java) { workoutScheduledJob.addRequest(request) }
        assertThat(workoutScheduledJob.getRequests()).hasSize(before)
    }

    @Test
    fun `old destructive schedules stay paused while new mirror schedules run`() {
        val service = mock(WorkoutService::class.java)
        val repository = mock(ScheduleRequestRepository::class.java)
        val mapper = ObjectMapperFactory.objectMapper()
        val oldMirror = C2CTodayScheduledRequest(
            listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )
        val legacy = oldMirror.copy(trainerRoadSyncMode = null, removeStaleTrainerRoad = true)
        val priorPolicy = oldMirror.copy(mirrorByTrainerRoadId = true)
        val allowed = priorPolicy.copy(allowUnidentifiedSourceRemoval = true)
        val addMissing = oldMirror.copy(trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING, allowStaleRemoval = false)
        `when`(repository.findAll()).thenReturn(listOf(
            ScheduleRequestEntity(1, """{"types":["BIKE"],"skipSynced":true,"sourcePlatform":"TRAINER_ROAD","targetPlatform":"INTERVALS","trainerRoadSyncMode":"MIRROR","allowStaleRemoval":true}"""),
            ScheduleRequestEntity(2, mapper.writeValueAsString(legacy)),
            ScheduleRequestEntity(3, mapper.writeValueAsString(allowed)),
            ScheduleRequestEntity(4, mapper.writeValueAsString(addMissing)),
            ScheduleRequestEntity(5, mapper.writeValueAsString(priorPolicy)),
        ))
        val today = LocalDate.now()
        `when`(service.copyWorkoutsC2C(allowed.forToday())).thenReturn(
            CopyWorkoutsResponse(0, 0, today, today, ExternalData.empty()),
        )
        `when`(service.copyWorkoutsC2C(addMissing.forToday())).thenReturn(
            CopyWorkoutsResponse(0, 0, today, today, ExternalData.empty()),
        )

        WorkoutScheduledJob(service, repository, mapper).job()

        verify(service, org.mockito.Mockito.never()).copyWorkoutsC2C(legacy.forToday())
        verify(service, org.mockito.Mockito.times(1)).copyWorkoutsC2C(allowed.forToday())
        verify(service).copyWorkoutsC2C(addMissing.forToday())
        for (id in 1..5) {
            val saved = org.mockito.Mockito.mockingDetails(repository).invocations.single {
                it.method.name == "updateLastResult" && it.arguments[0] == id
            }.arguments[1] as String
            assertThat(mapper.readValue(saved, ScheduledSyncResult::class.java).failed).isEqualTo(id <= 2 || id == 5)
        }
        assertThrows(IllegalArgumentException::class.java) {
            WorkoutScheduledJob(service, repository, mapper).addRequest(oldMirror)
        }
    }

    @Test
    fun `invalid and failing jobs do not prevent subsequent jobs`() {
        val service = mock(WorkoutService::class.java)
        val repository = mock(ScheduleRequestRepository::class.java)
        val mapper = ObjectMapperFactory.objectMapper()
        val old = C2CTodayScheduledRequest(
            listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS, refreshSynced = true,
        )
        val valid = C2CTodayScheduledRequest(listOf(TrainingType.BIKE), true, Platform.INTERVALS, Platform.TRAINING_PEAKS)
        `when`(repository.findAll()).thenReturn(listOf(
            ScheduleRequestEntity(1, "{invalid"),
            ScheduleRequestEntity(2, mapper.writeValueAsString(old)),
            ScheduleRequestEntity(3, mapper.writeValueAsString(valid)),
        ))
        org.mockito.Mockito.doThrow(PlatformException(Platform.INTERVALS, "Unsupported refresh"))
            .`when`(service).copyWorkoutsC2C(old.forToday())
        val today = LocalDate.now()
        val skipped = SkippedSyncWorkout(today, "Workout", "123", listOf("456"), SkippedSyncReason.PROTECTED_EVENT)
        `when`(service.copyWorkoutsC2C(valid.forToday())).thenReturn(
            CopyWorkoutsResponse(0, 0, today, today, ExternalData.empty(),
                skipped = 1, unidentifiedActivityDates = listOf(today), skippedWorkouts = listOf(skipped)),
        )

        WorkoutScheduledJob(service, repository, mapper).job()

        verify(service).copyWorkoutsC2C(valid.forToday())
        val saved = org.mockito.Mockito.mockingDetails(repository).invocations.single {
            it.method.name == "updateLastResult" && it.arguments[0] == 3
        }.arguments[1] as String
        val result = mapper.readValue(saved, ScheduledSyncResult::class.java)
        assertThat(result.failed).isFalse()
        assertThat(result.unidentifiedActivityDates).containsExactly(today)
        assertThat(result.skippedWorkouts).containsExactly(skipped)
    }
}
