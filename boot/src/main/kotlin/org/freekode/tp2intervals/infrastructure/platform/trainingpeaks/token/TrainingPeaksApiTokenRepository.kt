package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpAccessTokenCache"])
@Repository
class TrainingPeaksApiTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
) {
    @Cacheable(key = "'singleton'")
    fun getToken(): String {
        val authCookie = trainingPeaksConfigurationRepository.getConfiguration().authCookie
            ?: throw PlatformException(Platform.TRAINING_PEAKS, "Wrong configuration")
        val token = trainingPeaksTokenApiClient.getToken(authCookie)
        return token.accessToken!!
    }

}
