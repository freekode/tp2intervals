package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.CatchFeignException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.springframework.stereotype.Service

@Service
class WahooSystmConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        val updatedConfig = request.getByPrefix(platform().key)
        if (updatedConfig.isEmpty()) {
            return
        }
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(platform().key)
        val newConfig = currentConfig.configMap + updatedConfig
        validateConfiguration(newConfig, true)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    override fun isValid(): Boolean {
        try {
            val currentConfig =
                appConfigurationRepository.getConfigurationByPrefix(platform().key)
            validateConfiguration(currentConfig.configMap, false)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getConfiguration(): WahooSystmConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(platform().key)
        return WahooSystmConfiguration(config.configMap)
    }

    private fun validateConfiguration(newConfig: Map<String, String>, ignoreEmpty: Boolean) {
        val tpConfig = WahooSystmConfiguration(newConfig)
        if (!tpConfig.canValidate() && ignoreEmpty) {
            return
        }
        trainingPeaksTokenApiClient.getToken(tpConfig.username ?: "")
    }
}
