package org.freekode.tp2intervals.app.confguration

import jakarta.annotation.PostConstruct
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultConfigurationInitializer(
    private val configurationService: ConfigurationService,
    private val defaultConfigurationProperties: DefaultConfigurationProperties
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun init() {
        log.info("Init default configuration")
        defaultConfigurationProperties.defaultConfigurations.forEach {
            if (configurationService.getConfiguration(it.key) == null) {
                configurationService.updateConfiguration(UpdateConfigurationRequest(mapOf(Pair(it.key, it.value))))
            }
        }
    }
}
