package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.WorkoutService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping("/api/workout/plan-workout")
    fun planWorkout() {
        workoutService.planTodayAndTomorrowWorkouts()
    }

    @PostMapping("/api/workout/copy-plan")
    fun copyPlan(@RequestBody requestDTO: CopyPlanRequestDTO) {
        workoutService.copyPlanFromTrainingPeaks(
            LocalDate.parse(requestDTO.startDate),
            LocalDate.parse(requestDTO.endDate)
        )
    }
}
