package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.ProviderReadFailure
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpUserCache"])
@Repository
class TrainingPeaksUserRepository(
    private val trainingPeaksUserApiClient: TrainingPeaksUserApiClient,
) {
    @Cacheable(key = "'singleton'")
    fun getUser(): TrainingPeaksUser = resolveUser()

    fun resolveUser(): TrainingPeaksUser {
        val dto = trainingPeaksUserApiClient.getUser()
        val id = dto?.userId?.takeIf { it.isNotBlank() }
            ?: throw ProviderReadFailure(Platform.TRAINING_PEAKS, ProviderReadFailure.Kind.RESPONSE)
        return TrainingPeaksUser(id, dto.accountStatus.isAthlete, dto.accountStatus.isPremium)
    }
}
