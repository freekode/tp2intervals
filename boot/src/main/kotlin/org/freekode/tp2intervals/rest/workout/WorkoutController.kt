package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate
import org.freekode.tp2intervals.app.workout.CopyWorkoutsRequest
import org.freekode.tp2intervals.app.workout.PlanWorkoutsRequest
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.PlanType
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
        @RequestBody requestDTO: WorkoutsBaseRequestDTO,
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
        return PlanWorkoutsResponseDTO(
            response.planned,
            response.filteredOut,
            response.startDate.toString(),
            response.endDate.toString()
        )
    }

    @PostMapping("/api/workout/copy/{sourcePlatform}/{targetPlatform}")
    fun copyWorkouts(
        @RequestBody requestDTO: CopyWorkoutsRequestDTO,
        @PathVariable sourcePlatform: Platform,
        @PathVariable targetPlatform: Platform
    ): CopyWorkoutsResponseDTO {
        val response = workoutService.copyWorkouts(
            CopyWorkoutsRequest(
                "My Plan - ${requestDTO.startDate}",
                PlanType.PLAN,
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                requestDTO.types,
                sourcePlatform, targetPlatform
            )
        )
        return CopyWorkoutsResponseDTO(
            response.copied,
            response.filteredOut,
            response.startDate.toString(),
            response.endDate.toString()
        )
    }

}
