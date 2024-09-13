package org.freekode.tp2intervals.infrastructure.platform.trainerroad.cookie

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["trCookieCache"])
@Repository
class TRCookieRepository(
    private val trCookieApiClient: TRCookieApiClient,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository
) {
    @Cacheable(key = "'singleton'")
    fun getCookies(): String {
        val cookie = trainerRoadConfigurationRepository.getConfiguration().authCookie
            ?: throw PlatformException(Platform.TRAINER_ROAD, "Access to the platform is not configured")
//        val response = trCookieApiClient.getCookies(cookie)
//        return response.headers()[HttpHeaders.SET_COOKIE]!!
//            .map { it.split(";") }
//            .map { it[0].trim() }
//            .joinToString(separator = ";")
        return cookie
    }
}
