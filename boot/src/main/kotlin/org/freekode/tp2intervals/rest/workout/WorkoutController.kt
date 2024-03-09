package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate
import org.freekode.tp2intervals.app.workout.CopyWorkoutsRequest
import org.freekode.tp2intervals.app.workout.CopyWorkoutsResponse
import org.freekode.tp2intervals.app.workout.PlanWorkoutsRequest
import org.freekode.tp2intervals.app.workout.PlanWorkoutsResponse
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping("/api/workout/plan")
    fun planWorkout(@RequestBody requestDTO: PlanWorkoutsRequestDTO): PlanWorkoutsResponse {
        return workoutService.planWorkouts(
            PlanWorkoutsRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                requestDTO.types,
                requestDTO.skipSynced,
                requestDTO.sourcePlatform,
                requestDTO.targetPlatform
            )
        )
    }

    @GetMapping("/api/workout/find")
    fun findWorkoutsByName(@RequestParam platform: Platform, @RequestParam name: String): List<WorkoutDTO> {
        return workoutService.findWorkoutsByName(platform, name)
            .map { WorkoutDTO(it.name, it.duration, it.load, it.externalData) }
    }

    @PostMapping("/api/workout/copy")
    fun copyWorkouts(@RequestBody requestDTO: CopyWorkoutsRequestDTO): CopyWorkoutsResponse {
        return workoutService.copyWorkouts(
            CopyWorkoutsRequest(
                requestDTO.name,
                requestDTO.isPlan,
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                requestDTO.types,
                requestDTO.sourcePlatform,
                requestDTO.targetPlatform
            )
        )
    }

    @PostMapping("/api/workout/plan/schedule")
    fun addScheduledPlanWorkoutsRequest(@RequestBody requestDTO: PlanWorkoutsRequestDTO) {
        workoutService.addScheduledPlanWorkoutsRequest(
            PlanWorkoutsRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                requestDTO.types,
                requestDTO.skipSynced,
                requestDTO.sourcePlatform,
                requestDTO.targetPlatform
            )
        )
    }

    @GetMapping("/api/workout/plan/schedule")
    fun getScheduledPlanWorkoutsRequests(): PlanWorkoutsRequest? {
        return workoutService.getScheduledPlanWorkoutsRequest()
    }
}
