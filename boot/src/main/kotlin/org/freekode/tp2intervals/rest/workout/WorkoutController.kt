package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToLibraryRequest
import org.freekode.tp2intervals.app.workout.CopyFromLibraryToLibraryRequest
import org.freekode.tp2intervals.app.workout.CopyWorkoutsResponse
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutController(
    private val workoutService: WorkoutService,
) {
    @PostMapping("/api/workout/copy-calendar-to-calendar")
    fun copyWorkoutsFromCalendarToCalendar(@RequestBody request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        return workoutService.copyWorkoutsC2C(request)
    }

    @PostMapping("/api/workout/copy-calendar-to-library")
    fun copyWorkoutsFromCalendarToLibrary(@RequestBody request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        return workoutService.copyWorkoutsC2L(request)
    }

    @PostMapping("/api/workout/copy-library-to-library")
    fun copyWorkoutFromLibraryToLibrary(@RequestBody request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        return workoutService.copyWorkoutL2L(request)
    }

    @GetMapping("/api/workout/find")
    fun findWorkoutsByName(@RequestParam platform: Platform, @RequestParam name: String): List<WorkoutDetailsDTO> {
        return workoutService.findWorkoutsByName(platform, name)
            .map { workoutDetails ->
                WorkoutDetailsDTO(
                    workoutDetails.name,
                    workoutDetails.duration.toString().replace("PT", "").lowercase(),
                    workoutDetails.load,
                    workoutDetails.externalData
                )
            }
    }

    @DeleteMapping("/api/workout")
    fun deleteWorkoutsFromCalendar(@RequestBody request: DeleteWorkoutRequestDTO) {
        workoutService.deleteWorkoutsFromCalendar(request)
    }
}
