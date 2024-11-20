package org.freekode.tp2intervals.infrastructure.platform.strava

import org.freekode.tp2intervals.infrastructure.platform.strava.activity.StravaGetActivitiesResponseDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    value = "StravaApiClient",
    url = "\${app.strava.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [StravaApiClientConfig::class]
)
interface StravaApiClient {
    @GetMapping("/athlete/training_activities?page={page}")
    fun getActivities(
        @PathVariable page: Int,
    ): StravaGetActivitiesResponseDTO

    @PostMapping("/activities/{activityId}/export_original")
    fun exportOriginal(@PathVariable activityId: String): Resource
}
