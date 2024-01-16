package org.freekode.tp2intervals.infrastructure.trainingpeaks

import org.freekode.tp2intervals.app.ConnectionTester
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.infrastructure.trainingpeaks.token.TrainingPeaksApiTokenRepository
import org.springframework.stereotype.Component

@Component
class TrainingPeaksConnectionTester(
    private val trainingPeaksApiTokenRepository: TrainingPeaksApiTokenRepository,
) : ConnectionTester {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun test() {
        trainingPeaksApiTokenRepository.getTokenNow()
    }
}
