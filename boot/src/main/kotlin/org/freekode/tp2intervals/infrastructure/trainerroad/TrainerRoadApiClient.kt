package org.freekode.tp2intervals.infrastructure.trainerroad

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    value = "TrainerRoadApiClient",
    url = "\${trainer-road.api-url}",
    dismiss404 = true,
    configuration = [TrainerRoadApiClientConfig::class]
)
interface TrainerRoadApiClient {
    @GetMapping("/app/api/member-info")
    fun getMember(): TrainerRoadMemberDTO

    @GetMapping("/app/api/calendar/activities/{memberId}?startDate={startDate}&endDate={endDate}")
    fun getActivities(
        @PathVariable("memberId") memberId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String,
    ): List<Map<String, Any>>

}
