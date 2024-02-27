package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainerRoadConfiguration(
    val authCookie: String?,
) {
    companion object {
        const val CONFIG_PREFIX = "trainer-road"
        private const val authCookieKey = "${CONFIG_PREFIX}.auth-cookie"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String>) : this(map[authCookieKey])

    fun canValidate(): Boolean {
        return !authCookie.isNullOrBlank() && authCookie != "-1"
    }
}
