package org.freekode.tp2intervals.rest.acitivity

import java.time.LocalDate
import org.freekode.tp2intervals.app.activity.ActivityService
import org.freekode.tp2intervals.app.activity.SyncActivitiesRequest
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityService: ActivityService,
) {

    @PostMapping("/api/activity/sync/{sourcePlatform}/{targetPlatform}")
    fun syncActivities(
        @RequestBody requestDTO: SyncActivitiesRequestDTO,
        @PathVariable sourcePlatform: Platform,
        @PathVariable targetPlatform: Platform
    ) {
        activityService.syncActivities(
            SyncActivitiesRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                listOf(TrainingType.BIKE),
                sourcePlatform, targetPlatform
            )
        )
    }
}
