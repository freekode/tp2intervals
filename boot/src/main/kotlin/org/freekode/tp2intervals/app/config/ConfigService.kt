package org.freekode.tp2intervals.app.config

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ConfigService(
    configValidators: List<ConfigValidator>,
    private val appConfigRepository: AppConfigRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val configurationValidatorMap = configValidators.associateBy { it.platform() }

    fun findConfig(): AppConfig? = appConfigRepository.findConfig()

    fun updateConfig(appConfig: AppConfig) = appConfigRepository.updateConfig(appConfig)

    fun validateAllConfiguration(appConfig: AppConfig): List<String> {
        return configurationValidatorMap.keys.mapNotNull { validateConfiguration(it, appConfig) }
    }

    fun validateConfiguration(platform: Platform, appConfig: AppConfig): String? {
        try {
            configurationValidatorMap[platform]!!.validate(appConfig)
            return null
        } catch (e: Exception) {
            log.warn("Validation exception", e)
            return "${platform.title}: wrong configuration"
        }
    }
}
