package org.freekode.tp2intervals.infrastructure.trainerroad

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.trainerroad.configuraiton.TrainerRoadConfigurationRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class TrainerRoadApiClientConfig(
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = trainerRoadConfigurationRepository.getConfiguration().authCookie
            template.header(HttpHeaders.COOKIE, cookie)
        }
    }
}
