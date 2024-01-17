package org.freekode.tp2intervals.infrastructure.trainingpeaks.validator

import org.freekode.tp2intervals.app.config.ConfigValidator
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.infrastructure.trainingpeaks.token.TrainingPeaksApiTokenRepository
import org.springframework.stereotype.Component

@Component
class TrainingPeaksConfigValidator(
    private val trainingPeaksApiTokenRepository: TrainingPeaksApiTokenRepository,
) : ConfigValidator {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun validate(appConfig: AppConfig) {
        appConfig.tpConfig?.also { trainingPeaksApiTokenRepository.getToken(it.authCookie) }
    }
}
