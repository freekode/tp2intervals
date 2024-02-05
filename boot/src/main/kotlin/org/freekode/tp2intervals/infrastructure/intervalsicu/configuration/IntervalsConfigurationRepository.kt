package org.freekode.tp2intervals.infrastructure.intervalsicu.configuration

import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.stereotype.Service

@Service
class IntervalsConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository
) {
    companion object {
        const val CONFIG_PREFIX = "intervals"
    }

    fun getConfiguration(): IntervalsConfiguration {
        val configurationByKeyPrefix = appConfigurationRepository.getConfigurationByPrefix(CONFIG_PREFIX)
        return IntervalsConfiguration(configurationByKeyPrefix)
    }
}
