package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.WahooSystmConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["accessTokenCache"])
@Repository
class TrainingPeaksTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val wahooSystmConfigurationRepository: WahooSystmConfigurationRepository,
) {
    @Cacheable(key = "'TRAINING_PEAKS'")
    fun getToken(): String {
        val authCookie = wahooSystmConfigurationRepository.getConfiguration().username
            ?: throw PlatformException(Platform.TRAINING_PEAKS, "Wrong configuration")
        val token = trainingPeaksTokenApiClient.getToken(authCookie)
        return token.accessToken!!
    }

}
