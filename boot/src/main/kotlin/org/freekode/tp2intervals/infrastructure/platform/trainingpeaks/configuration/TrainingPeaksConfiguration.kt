package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest

data class TrainingPeaksConfiguration(
    val authCookie: String?,
) {
    companion object {
        const val CONFIG_PREFIX = "training-peaks"
        private const val authCookieKey = "${CONFIG_PREFIX}.auth-cookie"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.find(authCookieKey))

    fun merge(newConfig: Map<String, String>): TrainingPeaksConfiguration {
        return TrainingPeaksConfiguration(newConfig[authCookieKey])
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
