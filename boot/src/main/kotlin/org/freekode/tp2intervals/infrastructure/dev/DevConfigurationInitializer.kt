package org.freekode.tp2intervals.infrastructure.dev

import jakarta.annotation.PostConstruct
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile("dev")
class DevConfigurationInitializer(
    private val devConfiguration: DevConfiguration,
    private val appConfigurationRepository: AppConfigurationRepository
) {
    @PostConstruct
    fun initDevProperties() {
        val request = UpdateConfigurationRequest(devConfiguration.config)
        appConfigurationRepository.updateConfig(request)
    }
}
