package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration

import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.stereotype.Service

@Service
class IntervalsConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository
) {

    fun getConfiguration(): IntervalsConfiguration {
        val configurationByKeyPrefix = appConfigurationRepository.getConfigurationByPrefix(IntervalsConfiguration.CONFIG_PREFIX)
        return IntervalsConfiguration(configurationByKeyPrefix)
    }
}
