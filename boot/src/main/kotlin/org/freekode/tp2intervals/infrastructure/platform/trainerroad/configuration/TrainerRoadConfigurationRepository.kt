package org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberApiClient
import org.springframework.stereotype.Service

@Service
class TrainerRoadConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun updateConfig(request: UpdateConfigurationRequest) {
        val currentConfig = getConfiguration()
        val newConfig = currentConfig.merge(request.configMap)
        if (newConfig.canValidate()) {
            validateConfiguration(newConfig)
        }
        appConfigurationRepository.updateConfig(newConfig.getUpdateRequest())
    }

    fun getConfiguration(): TrainerRoadConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(TrainerRoadConfiguration.CONFIG_PREFIX)
        return TrainerRoadConfiguration(config)
    }

    private fun validateConfiguration(config: TrainerRoadConfiguration) {
        if (trainerRoadMemberApiClient.getMember(config.authCookie!!).MemberId == -1L) {
            throw PlatformException(Platform.TRAINER_ROAD, "Access Denied")
        }
    }

}
