package org.freekode.tp2intervals.infrastructure.trainingpeaks.token

import org.freekode.tp2intervals.infrastructure.trainingpeaks.user.TrainingPeaksUserTokenDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "ThirdPartyTokenApiClient",
    url = "\${third-party.api-url}",
    dismiss404 = true,
)
interface TrainingPeaksTokenApiClient {
    @GetMapping("/users/v3/token")
    fun getToken(@RequestHeader("Cookie") cookie: String): TrainingPeaksUserTokenDTO
}
