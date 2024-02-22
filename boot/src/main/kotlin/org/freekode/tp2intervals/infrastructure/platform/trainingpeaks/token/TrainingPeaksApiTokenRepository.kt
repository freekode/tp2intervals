package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token

import org.freekode.tp2intervals.infrastructure.trainingpeaks.configuraiton.TrainingPeaksConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpAccessToken"])
@Repository
class TrainingPeaksApiTokenRepository(
    private val trainingPeaksTokenApiClient: org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
) {
    @Cacheable(key = "'singleton'")
    fun getToken(): String {
        return getToken(trainingPeaksConfigurationRepository.getConfiguration().authCookie)
    }

    fun getToken(authCookie: String): String {
        val token = trainingPeaksTokenApiClient.getToken(authCookie)
        return token.accessToken!!
    }
}
