package org.freekode.tp2intervals.infrastructure.configuration

import jakarta.annotation.PostConstruct
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

    @PostConstruct
    fun initDevProperties() {
        if (defaultConfiguration.defaultConfig == null) {
            return
        }
        log.info("Initializing default configuration")
        val request = UpdateConfigurationRequest(defaultConfiguration.defaultConfig)
        appConfigurationRepository.updateConfig(request)
    }
}
