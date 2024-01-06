package org.freekode.tp2intervals.infrastructure.config

import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@CacheConfig(cacheNames = ["appConfiguration"])
@Repository
class AppConfigRepositoryImpl(
    private val h2ConfigRepository: H2ConfigRepository,
) : AppConfigRepository {

    @Cacheable(key = "'singleton'")
    override fun getConfig(): AppConfig {
        return findConfig() ?: throw IllegalStateException("There is no config")
    }

    @Cacheable(key = "'singleton'")
    override fun findConfig(): AppConfig? {
        val entity = h2ConfigRepository.findById(1).getOrNull() ?: return null
        return AppConfig(
            TrainingPeaksConfig(entity.tpAuthCookie!!),
            IntervalsConfig(entity.intervalsApiKey!!, entity.intervalsAthleteId!!)
        )
    }

    @CachePut(key = "'singleton'")
    override fun updateConfig(appConfig: AppConfig) {
        val entity = AppConfigEntity(
            1,
            appConfig.tpConfig.authCookie,
            appConfig.intervalsConfig.apiKey,
            appConfig.intervalsConfig.athleteId
        )
        h2ConfigRepository.save(entity)
    }
}
