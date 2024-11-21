package org.freekode.tp2intervals.rest.acitivity

import org.freekode.tp2intervals.app.activity.ActivityService
import org.freekode.tp2intervals.app.activity.CopyActivitiesRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityService: ActivityService,
) {

    @PostMapping("/api/activities/copy")
    fun syncActivities(@RequestBody request: CopyActivitiesRequest) =
        activityService.syncActivities(request)
}
