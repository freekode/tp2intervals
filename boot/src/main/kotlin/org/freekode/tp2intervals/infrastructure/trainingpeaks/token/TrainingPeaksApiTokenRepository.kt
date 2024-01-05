package org.freekode.tp2intervals.infrastructure.trainingpeaks.token

import org.freekode.tp2intervals.infrastructure.config.ConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpAccessToken"])
@Repository
class TrainingPeaksApiTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val configurationRepository: ConfigurationRepository
) {
    @Cacheable(key = "'singleton'")
    fun getToken(): String {
        val token = trainingPeaksTokenApiClient.getToken(configurationRepository.getConfiguration().tpAuthCookie)
        return token.accessToken!!
    }
}
