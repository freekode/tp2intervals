package org.freekode.tp2intervals.rest

import org.freekode.tp2intervals.app.WorkoutService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping("/api/plan-workout")
    fun planWorkout() {
        workoutService.planTodayAndTomorrowWorkouts()
    }

    @PostMapping("/api/copy-plan")
    fun copyPlan(@RequestBody requestDTO: CopyPlanRequestDTO) {
        workoutService.copyPlanFromThirdParty(
            LocalDate.parse(requestDTO.startDate),
            LocalDate.parse(requestDTO.endDate)
        )
    }
}
