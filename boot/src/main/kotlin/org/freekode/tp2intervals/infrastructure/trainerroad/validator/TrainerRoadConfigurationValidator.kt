package org.freekode.tp2intervals.infrastructure.trainerroad.validator

import org.freekode.tp2intervals.app.confguration.ConfigurationValidator
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.infrastructure.trainerroad.configuraiton.TrainerRoadConfiguration
import org.springframework.stereotype.Component

@Component
class TrainerRoadConfigurationValidator(
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient
) : ConfigurationValidator {
    override fun platform() = Platform.TRAINER_ROAD

    override fun validate(appConfiguration: AppConfiguration) {
        TrainerRoadConfiguration.tryToCreate(appConfiguration)?.also { validateConfig(it) }
    }

    private fun validateConfig(config: TrainerRoadConfiguration) {
        if (trainerRoadMemberApiClient.getMember(config.authCookie).MemberId == -1L) {
            throw IllegalStateException("Access Denied")
        }
    }
}
