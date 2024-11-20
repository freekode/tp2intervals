package org.freekode.tp2intervals.infrastructure.platform.strava.activity

import com.fasterxml.jackson.annotation.JsonProperty

class StravaGetActivitiesResponseDTO(
    val models: List<StravaActivityDTO>,
    val page: Int,
    val perPage: Int,
    val total: Int,
) {
    class StravaActivityDTO(
        val id: String,
        val name: String,
        @JsonProperty("start_time")
        val startTime: String,
        @JsonProperty("sport_type")
        val sportType: String,
    )
}
