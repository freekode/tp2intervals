package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.springframework.stereotype.Service

@Service
class TrainingPeaksConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun updateConfig(request: UpdateConfigurationRequest) {
        val currentConfig = getConfiguration()
        val newConfig = currentConfig.merge(request.configMap)
        if (newConfig.canValidate()) {
            validateConfiguration(newConfig)
        }
        appConfigurationRepository.updateConfig(newConfig.getUpdateRequest())
    }

    fun getConfiguration(): TrainingPeaksConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(TrainingPeaksConfiguration.CONFIG_PREFIX)
        return TrainingPeaksConfiguration(config)
    }

    private fun validateConfiguration(newConfig: TrainingPeaksConfiguration) {
        trainingPeaksTokenApiClient.getToken(newConfig.authCookie!!)
    }
}
