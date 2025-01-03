package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToLibraryRequest
import org.freekode.tp2intervals.app.workout.CopyFromLibraryToLibraryRequest
import org.freekode.tp2intervals.app.workout.CopyWorkoutsResponse
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutController(
    private val workoutService: WorkoutService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/api/workout/copy-calendar-to-calendar")
    fun copyWorkoutsFromCalendarToCalendar(@RequestBody request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to calendar: $request")
        return workoutService.copyWorkoutsFromCalendarToCalendar(request)
    }

    @PostMapping("/api/workout/copy-calendar-to-library")
    fun copyWorkoutsFromCalendarToLibrary(@RequestBody request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to library: $request")
        return workoutService.copyWorkoutsFromCalendarToLibrary(request)
    }

    @PostMapping("/api/workout/copy-library-to-library")
    fun copyWorkoutFromLibraryToLibrary(@RequestBody request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy library to library: $request")
        return workoutService.copyWorkoutFromLibraryToLibrary(request)
    }

    @GetMapping("/api/workout/find")
    fun findWorkoutsByName(@RequestParam platform: Platform, @RequestParam name: String): List<WorkoutDetailsDTO> {
        log.info("Received request for find workouts by name, platform: $platform, name: $name")
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
        log.info("Received request to delete workouts from calendar: $request")
        workoutService.deleteWorkoutsFromCalendar(request)
    }
}
