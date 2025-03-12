package org.freekode.tp2intervals.app.workout

import config.BaseSpringITConfig
import org.assertj.core.api.Assertions.assertThat
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class WorkoutJobSchedulerIT : BaseSpringITConfig() {
    @Autowired
    lateinit var workoutScheduledJob: WorkoutScheduledJob

    @Test
    fun test() {
        val request = CopyFromCalendarToCalendarRequest(LocalDate.now(), LocalDate.now(), listOf(TrainingType.BIKE), true, Platform.INTERVALS, Platform.TRAINING_PEAKS)

        workoutScheduledJob.addRequest(request)

        val requests = workoutScheduledJob.getRequests()

        assertThat(requests.isNotEmpty()).isTrue()
        assertThat(requests[0] is CopyFromCalendarToCalendarRequest).isTrue()
    }
}
