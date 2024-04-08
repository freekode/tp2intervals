package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration

import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "TrainerRoadValidationApiClient",
    url = "\${app.trainer-road.api-url}",
    dismiss404 = true,
)
interface TrainerRoadValidationApiClient {
    @GetMapping("/app/api/member-info")
    fun getMember(@RequestHeader(HttpHeaders.COOKIE) cookie: String): TrainerRoadMemberDTO
}
