package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuraiton

import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.stereotype.Service

@Service
class TrainingPeaksConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository
) {
    companion object {
        const val CONFIG_PREFIX = "training-peaks"
    }

    fun getConfiguration(): TrainingPeaksConfiguration {
        val configurationByKeyPrefix = appConfigurationRepository.getConfigurationByPrefix(CONFIG_PREFIX)
        return TrainingPeaksConfiguration(configurationByKeyPrefix)
    }
}
