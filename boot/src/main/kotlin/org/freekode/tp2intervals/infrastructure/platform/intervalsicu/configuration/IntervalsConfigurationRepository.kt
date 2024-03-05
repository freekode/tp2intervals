package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.CatchFeignException
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.utils.Auth
import org.springframework.stereotype.Service

@Service
class IntervalsConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val intervalsAthleteApiClient: IntervalsAthleteApiClient
) : PlatformConfigurationRepository {
    override fun platform() = Platform.INTERVALS

    @CatchFeignException(platform = Platform.INTERVALS)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        val newConfig = getConfigToUpdate(request)
        validateConfiguration(newConfig)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    fun getConfiguration(): IntervalsConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(IntervalsConfiguration.CONFIG_PREFIX)
        return IntervalsConfiguration(config)
    }

    private fun getConfigToUpdate(request: UpdateConfigurationRequest): Map<String, String> {
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(IntervalsConfiguration.CONFIG_PREFIX)
        return currentConfig.configMap + request.getByPrefix(IntervalsConfiguration.CONFIG_PREFIX)
    }


    private fun validateConfiguration(newConfig: Map<String, String>) {
        val intervalsConfig: IntervalsConfiguration
        try {
             intervalsConfig = IntervalsConfiguration(newConfig)
        } catch (e: NullPointerException) {
            throw PlatformException(platform(), "Wrong configuration")
        }

        intervalsAthleteApiClient.getAthlete(
            intervalsConfig.athleteId,
            Auth.getAuthorizationHeader(intervalsConfig.apiKey)
        )
    }
}
