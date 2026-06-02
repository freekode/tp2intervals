package org.freekode.tp2intervals.rest.wellness

import org.freekode.tp2intervals.app.wellness.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.wellness.schedule.ScheduledJobService
import org.springframework.web.bind.annotation.*

@RestController
class WellnessScheduledJobController(
    private val scheduledJob: ScheduledJobService
) {
    @PostMapping("/api/wellness/copy-calendar-to-calendar/schedule")
    fun scheduleC2CTodayRequest(@RequestBody request: C2CTodayScheduledRequest) {
        scheduledJob.addRequest(request)
    }

    @GetMapping("/api/wellness/copy-calendar-to-calendar/schedule")
    fun getScheduleRequests() =
        scheduledJob.getRequests()

    @DeleteMapping("/api/wellness/copy-calendar-to-calendar/schedule/{id}")
    fun deleteScheduleRequest(@PathVariable id: Int) =
        scheduledJob.deleteRequest(id)
}
