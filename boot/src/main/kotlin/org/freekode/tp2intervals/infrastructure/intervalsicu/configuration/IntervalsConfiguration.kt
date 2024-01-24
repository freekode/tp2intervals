package org.freekode.tp2intervals.infrastructure.intervalsicu.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration

data class IntervalsConfiguration(
    val apiKey: String,
    val athleteId: String
) {
    companion object {
        private const val apiKeyConfigKey = "${IntervalsConfigurationRepository.CONFIG_PREFIX}.api-key"
        private const val athleteIdConfigKey = "${IntervalsConfigurationRepository.CONFIG_PREFIX}.athlete-id"
    }

    constructor(appConfiguration: AppConfiguration) :
            this(appConfiguration.get(apiKeyConfigKey), appConfiguration.get(athleteIdConfigKey))
}
