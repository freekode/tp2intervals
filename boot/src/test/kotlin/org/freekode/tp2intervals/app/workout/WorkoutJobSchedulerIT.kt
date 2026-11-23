package org.freekode.tp2intervals.app.workout

import config.BaseSpringITConfig
import org.assertj.core.api.Assertions.assertThat
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
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

class WorkoutJobSchedulerIT : BaseSpringITConfig() {
    @Autowired
    lateinit var workoutScheduledJob: WorkoutScheduledJob

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
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
        )

        assertThrows(PlatformException::class.java) { workoutScheduledJob.addRequest(request) }
        assertThat(workoutScheduledJob.getRequests()).hasSize(before)
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

        WorkoutScheduledJob(service, repository, mapper).job()

        verify(service).copyWorkoutsC2C(valid.forToday())
    }
}
