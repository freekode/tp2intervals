package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user

import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpUserCache"])
@Repository
class TrainingPeaksUserRepository(
    private val trainingPeaksUserApiClient: TrainingPeaksUserApiClient,
) {
    @Cacheable(key = "'singleton'")
    fun getUser(): TrainingPeaksUser {
        val dto = trainingPeaksUserApiClient.getUser()
        return TrainingPeaksUser(dto.userId!!, dto.accountStatus.isAthlete, dto.accountStatus.isPremium)
    }
}
