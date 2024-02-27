package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsAthleteApiClient
import org.freekode.tp2intervals.infrastructure.utils.Auth
import org.springframework.stereotype.Service

@Service
class IntervalsConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val intervalsAthleteApiClient: IntervalsAthleteApiClient
) : PlatformConfigurationRepository {
    override fun platform() = Platform.INTERVALS

    override fun updateConfig(request: UpdateConfigurationRequest) {
        val currentConfig = getConfiguration()
        val newConfig = currentConfig.merge(request.configMap)
        validateConfiguration(newConfig)
        appConfigurationRepository.updateConfig(newConfig.getUpdateRequest())
    }

    fun getConfiguration(): IntervalsConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(IntervalsConfiguration.CONFIG_PREFIX)
        return IntervalsConfiguration(config)
    }

    private fun validateConfiguration(newConfig: IntervalsConfiguration) {
        intervalsAthleteApiClient.getAthlete(newConfig.athleteId, Auth.getAuthorizationHeader(newConfig.apiKey))
    }
}
