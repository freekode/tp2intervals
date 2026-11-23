package org.freekode.tp2intervals.app.workout

import config.BaseSpringITConfig
import org.assertj.core.api.Assertions.assertThat
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.beans.factory.annotation.Autowired

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
}
