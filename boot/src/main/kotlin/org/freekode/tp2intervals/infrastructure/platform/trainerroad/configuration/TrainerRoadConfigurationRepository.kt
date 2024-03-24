package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.CatchFeignException
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.springframework.stereotype.Service

@Service
class TrainerRoadConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainerRoadValidationApiClient: TrainerRoadValidationApiClient,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINER_ROAD

    @CatchFeignException(platform = Platform.TRAINER_ROAD)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        val updatedConfig = request.getByPrefix(TrainerRoadConfiguration.CONFIG_PREFIX)
        if (updatedConfig.isEmpty()) {
            return
        }
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(TrainerRoadConfiguration.CONFIG_PREFIX)
        val newConfig = currentConfig.configMap + updatedConfig
        validateConfiguration(newConfig, true)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    override fun isValid(): Boolean {
        try {
            val currentConfig =
                appConfigurationRepository.getConfigurationByPrefix(TrainerRoadConfiguration.CONFIG_PREFIX)
            validateConfiguration(currentConfig.configMap, false)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getConfiguration(): TrainerRoadConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(TrainerRoadConfiguration.CONFIG_PREFIX)
        return TrainerRoadConfiguration(config)
    }

    private fun validateConfiguration(newConfig: Map<String, String>, ignoreEmpty: Boolean) {
        val config = TrainerRoadConfiguration(newConfig)
        if (!config.canValidate() && ignoreEmpty) {
            return
        }
        if (trainerRoadValidationApiClient.getMember(config.authCookie ?: "").MemberId == -1L) {
            throw PlatformException(Platform.TRAINER_ROAD, "Access Denied")
        }
    }
}
