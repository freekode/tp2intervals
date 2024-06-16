package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.cookie.TRCookieRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class TrainerRoadApiClientConfig(
    private val trCookieRepository: TRCookieRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = trCookieRepository.getCookies()
            template.header(HttpHeaders.COOKIE, cookie)
        }
    }
}
