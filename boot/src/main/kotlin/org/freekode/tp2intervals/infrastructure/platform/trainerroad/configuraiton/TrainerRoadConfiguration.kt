package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuraiton

import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainerRoadConfiguration(
    val authCookie: String,
) {
    companion object {
        private const val authCookieKey = "${TrainerRoadConfigurationRepository.CONFIG_PREFIX}.auth-cookie"

        fun tryToCreate(appConfiguration: AppConfiguration): TrainerRoadConfiguration? {
            return if (appConfiguration.configMap.containsKey(authCookieKey)) {
                TrainerRoadConfiguration(appConfiguration.configMap[authCookieKey]!!)
            } else {
                null
            }
        }
    }

    constructor(appConfiguration: AppConfiguration) :
            this(appConfiguration.get(authCookieKey))
}
