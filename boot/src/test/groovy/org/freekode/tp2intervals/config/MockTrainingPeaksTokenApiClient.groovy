package org.freekode.tp2intervals.config


import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserTokenDTO

class MockTrainingPeaksTokenApiClient implements TrainingPeaksTokenApiClient {

    @Override
    TrainingPeaksUserTokenDTO getToken(String cookie) {
        if (cookie.isBlank()) {
            throw new IllegalArgumentException("Nah-ah")
        }
        return new TrainingPeaksUserTokenDTO("my-access-token")
    }
}
