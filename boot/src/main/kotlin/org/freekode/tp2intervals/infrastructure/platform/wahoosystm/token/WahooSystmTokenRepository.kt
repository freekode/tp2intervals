package org.freekode.tp2intervals.infrastructure.platform.wahoosystm.token

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.WahooSystmConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["accessTokenCache"])
@Repository
class WahooSystmTokenRepository(
    private val wahooSystmConfigurationRepository: WahooSystmConfigurationRepository,
) {
    @Cacheable(key = "'WAHOO_SYSTM'")
    fun getToken(): String {
        TODO("Not yet implemented")
    }

}
