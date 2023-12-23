package org.freekode.tp2intervals.infrastructure.thirdparty.token

import org.freekode.tp2intervals.infrastructure.thirdparty.user.ThirdPartyUserTokenDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "ThirdPartyTokenApiClient",
    url = "\${third-party.api-url}",
    dismiss404 = true,
)
interface ThirdPartyTokenApiClient {
    @GetMapping("/users/v3/token")
    fun getToken(@RequestHeader("Cookie") cookie: String): ThirdPartyUserTokenDTO
}
