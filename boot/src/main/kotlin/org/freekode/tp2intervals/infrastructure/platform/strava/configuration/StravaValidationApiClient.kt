package org.freekode.tp2intervals.infrastructure.platform.strava.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "StravaValidationApiClient",
    url = "\${app.strava.api-url}",
    dismiss404 = true,
    primary = false,
)
interface StravaValidationApiClient {
    @GetMapping("/frontend/athletes/current")
    fun getAthlete(@RequestHeader(HttpHeaders.COOKIE) cookie: String): StravaAthleteResponseDTO

    class StravaAthleteResponseDTO(
        var id: String?
    ) {
        @JsonProperty("currentAthlete")
        private fun mapId(map: Map<String, Any>?) {
            this.id = map?.get("id")?.toString()
        }
    }
}
