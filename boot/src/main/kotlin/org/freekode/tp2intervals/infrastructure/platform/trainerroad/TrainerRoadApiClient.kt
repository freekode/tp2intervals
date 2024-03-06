package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    value = "TrainerRoadApiClient",
    url = "\${trainer-road.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainerRoadApiClientConfig::class]
)
interface TrainerRoadApiClient {
    @GetMapping("/app/api/calendar/activities/{memberId}?startDate={startDate}&endDate={endDate}")
    fun getActivities(
        @PathVariable("memberId") memberId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String,
    ): List<TrainerRoadActivityDTO>

    @GetMapping("app/api/workoutdetails/{workoutId}")
    fun getWorkoutDetails(
        @PathVariable workoutId: String,
    ): TRWorkoutResponseDTO

    @PostMapping("/app/api/activities/{activityId}/exports/fit")
    fun exportFit(@PathVariable activityId: String): Resource
}
