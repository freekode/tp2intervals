package org.freekode.tp2intervals.infrastructure.intervalsicu.validator

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "IntervalsValidationApiClient",
    url = "\${intervals.api-url}",
    dismiss404 = true,
)
interface IntervalsValidationApiClient {

    @GetMapping("/api/v1/athlete/{athleteId}")
    fun getAthlete(
        @PathVariable("athleteId") athleteId: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String
    ): Map<String, Any>
}
