package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.validator

import org.freekode.tp2intervals.app.confguration.ConfigurationValidator
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.infrastructure.trainingpeaks.configuraiton.TrainingPeaksConfiguration
import org.freekode.tp2intervals.infrastructure.trainingpeaks.token.TrainingPeaksApiTokenRepository
import org.springframework.stereotype.Component

@Component
class TrainingPeaksConfigurationValidator(
    private val trainingPeaksApiTokenRepository: TrainingPeaksApiTokenRepository,
) : ConfigurationValidator {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun validate(appConfiguration: AppConfiguration) {
        TrainingPeaksConfiguration.tryToCreate(appConfiguration)
            ?.also { trainingPeaksApiTokenRepository.getToken(it.authCookie) }
    }
}
