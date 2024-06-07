package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainerRoadConfiguration(
    val authCookie: String?,
) {
    companion object {
        private val authCookieKey = "${Platform.TRAINER_ROAD.key}.auth-cookie"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String?>) : this(map[authCookieKey])

    fun canValidate(): Boolean {
        return authCookie != null
    }
}
