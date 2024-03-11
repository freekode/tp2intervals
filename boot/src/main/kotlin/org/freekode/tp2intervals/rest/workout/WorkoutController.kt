package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate
import org.freekode.tp2intervals.app.workout.CopyWorkoutsRequest
import org.freekode.tp2intervals.app.workout.CopyWorkoutsResponse
import org.freekode.tp2intervals.app.workout.ScheduleWorkoutsRequest
import org.freekode.tp2intervals.app.workout.ScheduleWorkoutsResponse
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

    @PostMapping("/api/workout/copy-planned")
    fun copyPlannedWorkouts(@RequestBody requestDTO: CopyPlannedRequestDTO): ScheduleWorkoutsResponse {
        return workoutService.copyPlannedWorkouts(
            ScheduleWorkoutsRequest(
                LocalDate.parse(requestDTO.startDate),
                LocalDate.parse(requestDTO.endDate),
                requestDTO.types,
                requestDTO.skipSynced,
                requestDTO.sourcePlatform,
                requestDTO.targetPlatform
            )
        )
    }

    @PostMapping("/api/workout/copy-planned-to-library")
    fun copyPlannedWorkoutsToLibrary(@RequestBody requestDTO: CopyPlannedToLibraryRequestDTO): CopyWorkoutsResponse {
        return workoutService.copyPlannedWorkoutsToLibrary(
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

    @GetMapping("/api/workout/find")
    fun findWorkoutsByName(@RequestParam platform: Platform, @RequestParam name: String): List<WorkoutDTO> {
        return workoutService.findWorkoutsByName(platform, name)
            .map { WorkoutDTO(it.name, it.duration, it.load, it.externalData) }
    }
}
