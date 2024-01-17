package org.freekode.tp2intervals.infrastructure.trainingpeaks.token

import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpAccessToken"])
@Repository
class TrainingPeaksApiTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val appConfigRepository: AppConfigRepository
) {
    @Cacheable(key = "'singleton'")
    fun getToken(): String {
        return getToken(appConfigRepository.getConfig().tpConfig!!.authCookie)
    }

    fun getToken(authCookie: String): String {
        val token = trainingPeaksTokenApiClient.getToken(authCookie)
        return token.accessToken!!
    }
}
