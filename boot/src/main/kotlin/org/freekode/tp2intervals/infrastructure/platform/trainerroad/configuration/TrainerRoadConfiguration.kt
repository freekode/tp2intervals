package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest

data class TrainerRoadConfiguration(
    val authCookie: String?,
) {
    companion object {
        const val CONFIG_PREFIX = "trainer-road"
        private const val authCookieKey = "${CONFIG_PREFIX}.auth-cookie"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.find(authCookieKey))

    fun merge(newConfig: Map<String, String>): TrainerRoadConfiguration {
        return TrainerRoadConfiguration(newConfig[authCookieKey])
    }

    fun canValidate(): Boolean {
        return !authCookie.isNullOrBlank() && authCookie != "-1"
    }

    fun getUpdateRequest(): UpdateConfigurationRequest {
        val configMap = mutableMapOf<String, String>()
        if (authCookie != null) {
            configMap[authCookieKey] = authCookie
        }
        return UpdateConfigurationRequest(configMap)
    }
}
