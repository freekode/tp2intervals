package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token

import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpAccessTokenCache"])
@Repository
class TrainingPeaksTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
) {
    @Cacheable(key = "#p1")
    fun getToken(authCookie: String, credentialKey: String): String = trainingPeaksTokenApiClient.getToken(authCookie).accessToken!!
}
