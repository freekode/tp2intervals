package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate
import org.freekode.tp2intervals.app.workout.CopyPlanRequest
import org.freekode.tp2intervals.app.workout.PlanWorkoutsRequest
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping("/api/workout/plan-workout/{sourcePlatform}/{targetPlatform}")
    fun planWorkout(
        @PathVariable sourcePlatform: Platform,
        @PathVariable targetPlatform: Platform
    ) {
        workoutService.planWorkouts(PlanWorkoutsRequest.fromTodayToTomorrow(sourcePlatform, targetPlatform))
    }

    @PostMapping("/api/workout/copy-plan/{sourcePlatform}/{targetPlatform}")
    fun copyPlan(
        @RequestBody requestDTO: CopyPlanRequestDTO,
        @PathVariable sourcePlatform: Platform,
        @PathVariable targetPlatform: Platform
    ) {
        workoutService.copyPlan(
            CopyPlanRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                sourcePlatform, targetPlatform
            )
        )
    }

}
