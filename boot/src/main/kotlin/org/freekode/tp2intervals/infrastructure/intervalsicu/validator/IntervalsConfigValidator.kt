package org.freekode.tp2intervals.infrastructure.intervalsicu.validator

import org.freekode.tp2intervals.app.config.ConfigValidator
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClientConfig
import org.springframework.stereotype.Component

@Component
class IntervalsConfigValidator(
    private val intervalsValidationApiClient: IntervalsValidationApiClient,
) : ConfigValidator {
    override fun platform() = Platform.INTERVALS

    override fun validate(appConfig: AppConfig) {
        intervalsValidationApiClient.getAthlete(
            appConfig.intervalsConfig.athleteId,
            IntervalsApiClientConfig.getAuthorizationHeader(appConfig.intervalsConfig.apiKey)
        )
    }
}
