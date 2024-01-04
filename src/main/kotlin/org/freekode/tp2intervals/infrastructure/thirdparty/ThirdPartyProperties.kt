package org.freekode.tp2intervals.infrastructure.thirdparty

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("third-party")
data class ThirdPartyProperties(
    val authCookie: String
)
