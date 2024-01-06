package org.freekode.tp2intervals.rest

import org.freekode.tp2intervals.app.WorkoutService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping("/api/plan-workout")
    fun planWorkout() {
        workoutService.planTodayAndTomorrowWorkouts()
    }
}
