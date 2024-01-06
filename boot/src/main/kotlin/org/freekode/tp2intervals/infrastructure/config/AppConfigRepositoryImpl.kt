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

    override fun findConfig(): AppConfig? {
        val entity = h2ConfigRepository.findById(1).getOrNull() ?: return null
        return toDomain(entity)
    }

    @CachePut(key = "'singleton'")
    override fun updateConfig(appConfig: AppConfig): AppConfig {
        val entity = AppConfigEntity(
            1,
            appConfig.tpConfig.authCookie,
            appConfig.intervalsConfig.apiKey,
            appConfig.intervalsConfig.athleteId
        )
        return toDomain(h2ConfigRepository.save(entity))
    }

    private fun toDomain(entity: AppConfigEntity) = AppConfig(
        TrainingPeaksConfig(entity.tpAuthCookie!!),
        IntervalsConfig(entity.intervalsApiKey!!, entity.intervalsAthleteId!!)
    )
}
