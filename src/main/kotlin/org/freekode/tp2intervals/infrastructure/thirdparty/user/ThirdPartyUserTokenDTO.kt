package org.freekode.tp2intervals.infrastructure.thirdparty.user

import com.fasterxml.jackson.annotation.JsonProperty

class ThirdPartyUserTokenDTO(
    var accessToken: String?
) {
    @JsonProperty("token")
    private fun mapAccessToken(map: Map<String, Any>) {
        this.accessToken = map["access_token"].toString()
    }
}
