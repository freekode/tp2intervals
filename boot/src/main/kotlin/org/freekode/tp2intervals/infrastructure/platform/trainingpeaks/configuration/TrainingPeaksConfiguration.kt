package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainingPeaksConfiguration(
    val authCookie: String?,
) {
    companion object {
        const val CONFIG_PREFIX = "training-peaks"
        private const val authCookieKey = "${CONFIG_PREFIX}.auth-cookie"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String>) : this(map[authCookieKey])

    fun canValidate(): Boolean {
        return authCookie != null && authCookie != "-1"
    }
}
