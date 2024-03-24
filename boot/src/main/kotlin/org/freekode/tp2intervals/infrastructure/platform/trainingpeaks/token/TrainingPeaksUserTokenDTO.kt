package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token

import com.fasterxml.jackson.annotation.JsonProperty

class TrainingPeaksUserTokenDTO(
    var accessToken: String?
) {
    @JsonProperty("token")
    private fun mapAccessToken(map: Map<String, Any>) {
        this.accessToken = map["access_token"].toString()
    }
}
