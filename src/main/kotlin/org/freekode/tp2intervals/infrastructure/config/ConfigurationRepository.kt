package org.freekode.tp2intervals.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["appConfiguration"])
@Repository
class ConfigurationRepository(
    @Value("\${training-peaks.auth-cookie}") private val tpAuthCookie: String,
    @Value("\${intervals.login}") private val intervalsLogin: String,
    @Value("\${intervals.password}") private val intervalsPassword: String,
    @Value("\${intervals.athlete-id}") private val intervalsAthleteId: String,
) {
    @Cacheable(key = "'singleton'")
    fun getConfiguration(): AppConfiguration {
        return AppConfiguration(tpAuthCookie, intervalsLogin, intervalsPassword, intervalsAthleteId)
    }
}
