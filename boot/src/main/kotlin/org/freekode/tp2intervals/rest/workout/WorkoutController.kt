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

    @PostMapping("/api/workout/plan/{sourcePlatform}/{targetPlatform}")
    fun planWorkout(
        @RequestBody requestDTO: DateRangeDTO,
        @PathVariable sourcePlatform: Platform,
        @PathVariable targetPlatform: Platform
    ): PlanWorkoutsResponseDTO {
        val response = workoutService.planWorkouts(
            PlanWorkoutsRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                requestDTO.types,
                sourcePlatform,
                targetPlatform
            )
        )
        return PlanWorkoutsResponseDTO(response.planned, response.filteredOut, response.startDate, response.endDate)
    }

    @PostMapping("/api/workout/copy/{sourcePlatform}/{targetPlatform}")
    fun copyPlan(
        @RequestBody requestDTO: DateRangeDTO,
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
