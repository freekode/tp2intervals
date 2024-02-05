package org.freekode.tp2intervals.app.confguration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ConfigurationService(
    configurationValidators: List<ConfigurationValidator>,
    private val appConfigurationRepository: AppConfigurationRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val configurationValidatorMap = configurationValidators.associateBy { it.platform() }

    fun getConfiguration(key: String): String? = appConfigurationRepository.getConfiguration(key)

    fun getConfigurations(): AppConfiguration = appConfigurationRepository.getConfigurations()

    fun updateConfiguration(request: UpdateConfigurationRequest) = appConfigurationRepository.updateConfig(request)

    fun validateAllConfiguration(appConfiguration: AppConfiguration): List<String> {
        return configurationValidatorMap.keys.mapNotNull { validateConfiguration(it, appConfiguration) }
    }

    fun validateConfiguration(platform: Platform, appConfiguration: AppConfiguration): String? {
        try {
            configurationValidatorMap[platform]!!.validate(appConfiguration)
            return null
        } catch (e: Exception) {
            log.warn("Validation exception", e)
            return "${platform.title}: wrong configuration"
        }
    }
}
