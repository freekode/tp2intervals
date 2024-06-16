package org.freekode.tp2intervals.infrastructure.platform.trainerroad.cookie

import feign.Response
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "TRCookieApiClient",
    url = "\${app.trainer-road.api-url}",
    dismiss404 = true,
    primary = false,
)
interface TRCookieApiClient {
    @GetMapping("/app/career")
    fun getCookies(@RequestHeader("Cookie") cookie: String): Response
}
