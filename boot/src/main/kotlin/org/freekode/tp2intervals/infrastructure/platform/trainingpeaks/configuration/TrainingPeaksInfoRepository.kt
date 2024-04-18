package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.PlatformInfo
import org.freekode.tp2intervals.domain.config.PlatformInfoRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["platformInfoCache"])
class TrainingPeaksInfoRepository(
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
) : PlatformInfoRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    @Cacheable(key = "'training-peaks'")
    override fun platformInfo(): PlatformInfo {
        val isValid = trainingPeaksConfigurationRepository.isValid()
        val isAthlete = if (isValid) trainingPeaksUserRepository.getUser().isAthlete else false
        val infoMap = mapOf(
            "isValid" to isValid,
            "isAthlete" to isAthlete
        )
        return PlatformInfo(infoMap)
    }
}
