package org.freekode.tp2intervals.infrastructure.trainerroad

import feign.RequestInterceptor
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class TrainerRoadApiClientConfig(
    private val appConfigRepository: AppConfigRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = appConfigRepository.getConfig().trConfig.authCookie
            template.header(HttpHeaders.COOKIE, cookie)
        }
    }
}
