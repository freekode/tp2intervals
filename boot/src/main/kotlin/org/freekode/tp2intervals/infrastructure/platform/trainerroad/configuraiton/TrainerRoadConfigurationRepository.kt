package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuraiton

import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.stereotype.Service

@Service
class TrainerRoadConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository
) {
    companion object {
        const val CONFIG_PREFIX = "trainer-road"
    }

    fun getConfiguration(): TrainerRoadConfiguration {
        val configurationByKeyPrefix = appConfigurationRepository.getConfigurationByPrefix(CONFIG_PREFIX)
        return TrainerRoadConfiguration(configurationByKeyPrefix)
    }
}
