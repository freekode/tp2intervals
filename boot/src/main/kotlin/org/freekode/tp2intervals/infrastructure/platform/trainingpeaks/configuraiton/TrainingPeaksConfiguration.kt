package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuraiton

import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainingPeaksConfiguration(
    val authCookie: String,
) {
    companion object {
        private const val authCookieKey =
            "${TrainingPeaksConfigurationRepository.CONFIG_PREFIX}.auth-cookie"

        fun tryToCreate(appConfiguration: AppConfiguration): TrainingPeaksConfiguration? {
            return if (appConfiguration.configMap.containsKey(authCookieKey)) {
                TrainingPeaksConfiguration(
                    appConfiguration.configMap[authCookieKey]!!
                )
            } else {
                null
            }
        }
    }

    constructor(appConfiguration: AppConfiguration) :
            this(appConfiguration.get(authCookieKey))
}
