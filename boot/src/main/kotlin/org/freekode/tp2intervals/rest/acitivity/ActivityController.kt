package org.freekode.tp2intervals.rest.acitivity

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.app.activity.ActivityService
import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class ActivityController(
    private val activityService: ActivityService
) {

    @PutMapping("/api/activity/sync-workouts/trainer-road/intervals")
    fun syncActivities(@RequestBody requestDTO: SyncActivitiesRequestDTO) {
        activityService.syncActivities(
            LocalDate.parse(requestDTO.date), listOf(TrainingType.BIKE),
            Platform.TRAINER_ROAD, Platform.INTERVALS
        )
    }
}
