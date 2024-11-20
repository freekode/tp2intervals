package org.freekode.tp2intervals.infrastructure.platform.strava.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration

data class StravaConfiguration(
    val cookie: String?,
) {
    companion object {
        private val cookieKey = "${Platform.STRAVA.key}.cookie"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String?>) : this(
        map[cookieKey],
    )

    fun canValidate(): Boolean {
        return cookie != null
    }
}
