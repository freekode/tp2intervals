package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import feign.RequestInterceptor
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.cookie.TRCookieRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class TrainerRoadApiClientConfig(
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository

) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = trainerRoadConfigurationRepository.getConfiguration().authCookie
                ?: throw PlatformException(Platform.TRAINER_ROAD, "Access to the platform is not configured")
            template.header(HttpHeaders.COOKIE, cookie)
        }
    }
}
