package org.freekode.tp2intervals.infrastructure.trainerroad.validator

import org.freekode.tp2intervals.app.config.ConfigValidator
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.TrainerRoadConfig
import org.springframework.stereotype.Component

@Component
class TrainerRoadConfigValidator(
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient
) : ConfigValidator {
    override fun platform() = Platform.TRAINER_ROAD

    override fun validate(appConfig: AppConfig) {
        appConfig.trConfig?.also { validateConfig(it) }
    }

    private fun validateConfig(it: TrainerRoadConfig) {
        if (trainerRoadMemberApiClient.getMember(it.authCookie).MemberId == -1L) {
            throw IllegalStateException("Access Denied")
        }
    }
}
