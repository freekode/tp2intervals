package org.freekode.tp2intervals.rest.acitivity

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.app.activity.ActivityService
import org.freekode.tp2intervals.app.activity.ActivitySyncJobScheduler
import org.freekode.tp2intervals.app.activity.SyncActivitiesRequest
import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityService: ActivityService,
    private val activitySyncJobScheduler: ActivitySyncJobScheduler
) {

    @PutMapping("/api/activity/sync-workouts/trainer-road/intervals")
    fun syncActivities(@RequestBody requestDTO: SyncActivitiesRequestDTO) {
        activityService.syncActivities(
            SyncActivitiesRequest(
                LocalDate.parse(requestDTO.startDate), LocalDate.parse(requestDTO.endDate),
                listOf(TrainingType.BIKE),
                Platform.TRAINER_ROAD, Platform.INTERVALS
            )
        )
    }

    @PostMapping("/api/activity/sync-workouts/trainer-road/intervals/job")
    fun startJobSyncActivities() {
        activitySyncJobScheduler.startJob(Platform.TRAINER_ROAD, Platform.INTERVALS)
    }

    @GetMapping("/api/activity/sync-workouts/trainer-road/intervals/job")
    fun checkJobSyncActivities() {
        activitySyncJobScheduler.checkJob()
    }

    @DeleteMapping("/api/activity/sync-workouts/trainer-road/intervals/job")
    fun stopJobSyncActivities() {
        activitySyncJobScheduler.endJob()
    }
}
