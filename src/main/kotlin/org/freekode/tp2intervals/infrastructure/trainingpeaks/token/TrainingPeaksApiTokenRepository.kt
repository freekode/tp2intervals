package org.freekode.tp2intervals.infrastructure.trainingpeaks.token

import org.freekode.tp2intervals.infrastructure.trainingpeaks.TrainingPeaksProperties
import org.springframework.stereotype.Repository

@Repository
class TrainingPeaksApiTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val trainingPeaksProperties: TrainingPeaksProperties
) {
    fun getToken(): String {
        val token = trainingPeaksTokenApiClient.getToken(trainingPeaksProperties.authCookie)
        return token.accessToken!!
    }
}
