package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.scheduled.CopyFromCalendarToCalendarScheduledRequest
import org.freekode.tp2intervals.app.workout.scheduled.WorkoutJobScheduler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutJobSchedulerController(
    private val workoutJobScheduler: WorkoutJobScheduler
) {
    @PostMapping("/api/workout/copy-calendar-to-calendar/schedule")
    fun scheduleCopyWorkoutsFromCalendarToCalendar(@RequestBody request: CopyFromCalendarToCalendarRequest) {
        return workoutJobScheduler.addRequest(CopyFromCalendarToCalendarScheduledRequest(request))
    }

    @GetMapping("/api/workout/copy-calendar-to-calendar/schedule")
    fun getScheduledJobsCopyWorkoutsFromCalendarToCalendar() =
        workoutJobScheduler.getRequests()
}
