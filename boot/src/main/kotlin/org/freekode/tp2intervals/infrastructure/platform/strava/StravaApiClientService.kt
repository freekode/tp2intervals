package org.freekode.tp2intervals.infrastructure.platform.strava

import org.freekode.tp2intervals.infrastructure.platform.strava.activity.StravaGetActivitiesResponseDTO
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class StravaApiClientService(
    private val stravaApiClient: StravaApiClient
) {
    fun getActivities(page: Int): StravaGetActivitiesResponseDTO =
        stravaApiClient.getActivities(page)

    fun exportOriginal(activityId: String): Resource? {
        val response = stravaApiClient.exportOriginal(activityId)
        if (response.headers.contentType == MediaType.APPLICATION_OCTET_STREAM) {
            return response.body
        }
        return null
    }
}
