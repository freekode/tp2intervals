package org.freekode.tp2intervals.rest.acitivity

import java.time.LocalDate
import org.freekode.tp2intervals.app.activity.ActivityService
import org.freekode.tp2intervals.app.activity.SyncActivitiesRequest
import org.freekode.tp2intervals.domain.Platform
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
) {

    @PutMapping("/api/activity/sync-activities/trainer-road/intervals")
    fun syncActivities(@RequestBody requestDTO: SyncActivitiesRequestDTO) {
        activityService.syncActivities(
            getSyncRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate)
            )
        )
    }

    @PostMapping("/api/activity/sync-activities/trainer-road/intervals/job")
    fun startJobSyncActivities() {
        activityService.scheduleSyncActivitiesJob(getSyncRequest(LocalDate.now(), LocalDate.now()))
    }

    @GetMapping("/api/activity/sync-activities/trainer-road/intervals/job")
    fun getJobSyncActivities(): JobDTO? {
        return activityService.getJob(Platform.TRAINER_ROAD, Platform.INTERVALS)?.let { JobDTO(it.id) }
    }

    @DeleteMapping("/api/activity/sync-activities/trainer-road/intervals/job")
    fun stopJobSyncActivities() {
        activityService.stopJob(Platform.TRAINER_ROAD, Platform.INTERVALS)
    }

    private fun getSyncRequest(startDate: LocalDate, endDate: LocalDate) =
        SyncActivitiesRequest(
            startDate, endDate,
            listOf(TrainingType.BIKE),
            Platform.TRAINER_ROAD, Platform.INTERVALS
        )
}
