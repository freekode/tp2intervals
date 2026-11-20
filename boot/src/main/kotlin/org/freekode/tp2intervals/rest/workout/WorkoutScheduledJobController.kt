package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutScheduledJobController(
    private val workoutScheduledJob: WorkoutScheduledJob
) {
    @PostMapping("/api/workout/copy-calendar-to-calendar/schedule")
    fun scheduleC2CTodayRequest(@RequestBody request: String) {
        try {
            workoutScheduledJob.addRequest(request)
        } catch (error: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, error.message)
        }
    }

    @GetMapping("/api/workout/copy-calendar-to-calendar/schedule")
    fun getScheduleRequests() =
        workoutScheduledJob.auditRequests()

    @DeleteMapping("/api/workout/copy-calendar-to-calendar/schedule/{id}")
    fun deleteScheduleRequest(@PathVariable id: Int) =
        workoutScheduledJob.deleteRequest(id)
}
