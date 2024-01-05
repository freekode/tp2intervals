package org.freekode.tp2intervals.infrastructure.config

import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["appConfiguration"])
@Repository
class AppConfigRepositoryImpl(
    @Value("\${training-peaks.auth-cookie}") private val tpAuthCookie: String,
    @Value("\${intervals.login}") private val intervalsLogin: String,
    @Value("\${intervals.password}") private val intervalsPassword: String,
    @Value("\${intervals.athlete-id}") private val intervalsAthleteId: String,
) : AppConfigRepository {
    @Cacheable(key = "'singleton'")
    override fun getConfig(): AppConfig {
        return AppConfig(
            TrainingPeaksConfig(tpAuthCookie),
            IntervalsConfig(intervalsLogin, intervalsPassword, intervalsAthleteId)
        )
    }

    override fun updateConfig(appConfig: AppConfig) {

    }
}
