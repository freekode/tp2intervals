package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.metrics.TPMetricsDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.CreateTPWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPNoteResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutDetailsResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.*

@FeignClient(
    value = "TrainingPeaksApiClient",
    url = "\${app.training-peaks.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class]
)
interface TrainingPeaksApiClient {
    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{startDate}/{endDate}")
    fun getWorkouts(
        @PathVariable("userId") userId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String
    ): List<TPWorkoutCalendarResponseDTO>

    @GetMapping("/fitness/v1/athletes/{userId}/calendarNote/{startDate}/{endDate}")
    fun getNotes(
        @PathVariable("userId") userId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String
    ): List<TPNoteResponseDTO>

    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}/fordevice/fit")
    fun downloadWorkoutFit(
        @PathVariable("userId") userId: String,
        @PathVariable("workoutId") workoutId: String,
    ): Resource

    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}/details")
    fun getWorkoutDetails(
        @PathVariable userId: String,
        @PathVariable workoutId: String,
    ): TPWorkoutDetailsResponseDTO

    @GetMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}/attachments/{attachmentId}/raw")
    fun downloadWorkoutAttachment(
        @PathVariable userId: String,
        @PathVariable workoutId: String,
        @PathVariable attachmentId: String,
    ): Resource

    @PostMapping("/fitness/v6/athletes/{userId}/workouts")
    fun createAndPlanWorkout(
        @PathVariable("userId") userId: String,
        @RequestBody requestDTO: CreateTPWorkoutRequestDTO
    )

    @DeleteMapping("/fitness/v6/athletes/{userId}/workouts/{workoutId}")
    fun deleteWorkout(
        @PathVariable userId: String,
        @PathVariable workoutId: String,
    ): Boolean

    @GetMapping("/metrics/v3/athletes/{athleteId}/consolidatedtimedmetrics/{startDate}/{endDate}")
    fun getMetrics(
        @PathVariable("athleteId") userId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String
    ): List<TPMetricsDTO>

    @PutMapping("/metrics/v3/athletes/{athleteId}/consolidatedtimedmetric")
    fun createMetrics(
        @PathVariable athleteId: String,
        @RequestBody requestDTO: TPMetricsDTO
    )

    @DeleteMapping("/metrics/v3/athletes/{athleteId}/consolidatedtimedmetric")
    fun deleteMetrics(
        @PathVariable athleteId: String,
        @RequestBody requestDTO: TPMetricsDTO
    )
}
