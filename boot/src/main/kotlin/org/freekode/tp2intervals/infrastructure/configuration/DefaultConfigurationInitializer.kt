package org.freekode.tp2intervals.infrastructure.configuration

import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class DefaultConfigurationInitializer(
    private val defaultConfiguration: DefaultConfiguration,
    private val appConfigurationRepository: AppConfigurationRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    init {
        initDefaultProperties()
    }

    private fun initDefaultProperties() {
        if (defaultConfiguration.defaultConfig == null) {
            log.info("Default configuration is empty")
            return
        }
        log.info("Initializing default configuration")
        val request = UpdateConfigurationRequest(defaultConfiguration.defaultConfig)
        appConfigurationRepository.updateConfig(request)
    }
}
