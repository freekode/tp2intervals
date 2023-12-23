package org.freekode.tp2intervals.infrastructure.thirdparty.token

import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyProperties
import org.springframework.stereotype.Repository

@Repository
class ThirdPartyApiTokenRepository(
    private val thirdPartyTokenApiClient: ThirdPartyTokenApiClient,
    private val thirdPartyProperties: ThirdPartyProperties
) {
    fun getToken(): String {
        val token = thirdPartyTokenApiClient.getToken(thirdPartyProperties.cookies.entries.joinToString("&"))
        return token.accessToken!!
    }
}
