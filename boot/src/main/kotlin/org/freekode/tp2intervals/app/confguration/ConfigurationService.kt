package org.freekode.tp2intervals.app.confguration

import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.springframework.stereotype.Service

@Service
class ConfigurationService(
    private val platformConfigurationRepositories: List<PlatformConfigurationRepository>,
    private val appConfigurationRepository: AppConfigurationRepository
) {
    fun getConfiguration(key: String): String? = appConfigurationRepository.getConfiguration(key)

    fun getConfigurations(): AppConfiguration = appConfigurationRepository.getConfigurations()

    fun updateConfiguration(request: UpdateConfigurationRequest): List<String> {
        return platformConfigurationRepositories.mapNotNull { updateConfiguration(request, it) }
    }

    private fun updateConfiguration(
        request: UpdateConfigurationRequest,
        repository: PlatformConfigurationRepository
    ): String? {
        try {
            repository.updateConfig(request)
            return null
        } catch (e: PlatformException) {
            return "${e.platform.title}: ${e.message}"
        }
    }
}
