package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate
import org.freekode.tp2intervals.app.workout.CopyPlanRequest
import org.freekode.tp2intervals.app.workout.PlanWorkoutsRequest
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.rest.acitivity.JobDTO
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping("/api/workout/plan-workout/intervals/training-peaks")
    fun planWorkout() {
        workoutService.planWorkouts(getPlanRequest())
    }

    @PostMapping("/api/workout/plan-workout/intervals/training-peaks/job")
    fun startJobPlanWorkout() {
        workoutService.schedulePlanWorkoutsJob(getPlanRequest())
    }

    @GetMapping("/api/workout/plan-workout/intervals/training-peaks/job")
    fun getJobPlanWorkout(): JobDTO? {
        return workoutService.getJob(Platform.INTERVALS, Platform.TRAINING_PEAKS)?.let { JobDTO(it) }
    }

    @DeleteMapping("/api/workout/plan-workout/intervals/training-peaks/job")
    fun stopJobPlanWorkout() {
        workoutService.stopJob(Platform.INTERVALS, Platform.TRAINING_PEAKS)
    }

    @PostMapping("/api/workout/copy-plan/training-peaks/intervals")
    fun copyPlan(@RequestBody requestDTO: CopyPlanRequestDTO) {
        workoutService.copyPlan(
            CopyPlanRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                Platform.TRAINING_PEAKS,
                Platform.INTERVALS
            )
        )
    }

    private fun getPlanRequest() =
        PlanWorkoutsRequest.fromTodayToTomorrow(
            Platform.INTERVALS, Platform.TRAINING_PEAKS
        )
}
