package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user

import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpUserId"])
@Repository
class TrainingPeaksUserRepository(
    private val trainingPeaksUserApiClient: TrainingPeaksUserApiClient,
) {
    @Cacheable(key = "'singleton'")
    fun getUserId(): String {
        return trainingPeaksUserApiClient.getUser().userId!!
    }
}
