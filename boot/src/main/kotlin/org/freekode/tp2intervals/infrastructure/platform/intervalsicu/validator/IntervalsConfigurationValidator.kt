package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.validator

import org.freekode.tp2intervals.app.confguration.ConfigurationValidator
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClientConfig
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.springframework.stereotype.Component

@Component
class IntervalsConfigurationValidator(
    private val intervalsValidationApiClient: org.freekode.tp2intervals.infrastructure.platform.intervalsicu.validator.IntervalsValidationApiClient,
) : ConfigurationValidator {
    override fun platform() = Platform.INTERVALS

    override fun validate(appConfiguration: AppConfiguration) {
        val config = IntervalsConfiguration(appConfiguration)

        intervalsValidationApiClient.getAthlete(
            config.athleteId,
            IntervalsApiClientConfig.getAuthorizationHeader(config.apiKey)
        )
    }
}
