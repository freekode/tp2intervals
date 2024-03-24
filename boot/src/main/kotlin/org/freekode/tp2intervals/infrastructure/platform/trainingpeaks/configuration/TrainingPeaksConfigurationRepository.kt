package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.CatchFeignException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.springframework.stereotype.Service

@Service
class TrainingPeaksConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        val updatedConfig = request.getByPrefix(TrainingPeaksConfiguration.CONFIG_PREFIX)
        if (updatedConfig.isEmpty()) {
            return
        }
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(TrainingPeaksConfiguration.CONFIG_PREFIX)
        val newConfig = currentConfig.configMap + updatedConfig
        validateConfiguration(newConfig, true)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    override fun isValid(): Boolean {
        try {
            val currentConfig =
                appConfigurationRepository.getConfigurationByPrefix(TrainingPeaksConfiguration.CONFIG_PREFIX)
            validateConfiguration(currentConfig.configMap, false)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getConfiguration(): TrainingPeaksConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(TrainingPeaksConfiguration.CONFIG_PREFIX)
        return TrainingPeaksConfiguration(config)
    }

    private fun validateConfiguration(newConfig: Map<String, String>, ignoreEmpty: Boolean) {
        val tpConfig = TrainingPeaksConfiguration(newConfig)
        if (!tpConfig.canValidate() && ignoreEmpty) {
            return
        }
        trainingPeaksTokenApiClient.getToken(tpConfig.authCookie ?: "")
    }
}
