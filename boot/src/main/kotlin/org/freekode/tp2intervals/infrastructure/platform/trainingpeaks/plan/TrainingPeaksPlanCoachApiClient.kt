package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClientConfig
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPNoteResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    value = "TrainingPeaksPlanCoachApiClient",
    url = "\${app.training-peaks.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class]
)
interface TrainingPeaksPlanCoachApiClient {
    @GetMapping("/plans/v1/plans/{planId}/workouts/{startDate}/{endDate}")
    fun getPlanWorkouts(
        @PathVariable planId: String,
        @PathVariable startDate: String,
        @PathVariable endDate: String,
    ): List<TPWorkoutResponseDTO>

    @GetMapping("/plans/v1/plans/{planId}/calendarNote/{startDate}/{endDate}")
    fun getPlanNotes(
        @PathVariable planId: String,
        @PathVariable startDate: String,
        @PathVariable endDate: String,
    ): List<TPNoteResponseDTO>

    @GetMapping("/plans/v1/plans/{planId}/events/{startDate}/{endDate}")
    fun getPlanEvents(
        @PathVariable planId: String,
        @PathVariable startDate: String,
        @PathVariable endDate: String,
    ): Map<String, Any>

}
