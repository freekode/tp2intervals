package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserTokenDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "TrainingPeaksTokenApiClient",
    url = "\${training-peaks.api-url}",
    dismiss404 = true,
)
interface TrainingPeaksTokenApiClient {
    @GetMapping("/users/v3/token")
    fun getToken(@RequestHeader(HttpHeaders.COOKIE) cookie: String): org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserTokenDTO
}
