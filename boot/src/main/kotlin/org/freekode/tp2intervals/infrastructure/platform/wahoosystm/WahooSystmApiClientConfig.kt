package org.freekode.tp2intervals.infrastructure.platform.wahoosystm

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.WahooSystmTokenRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders


class WahooSystmApiClientConfig(
    private val wahooSystmTokenRepository: WahooSystmTokenRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val token = wahooSystmTokenRepository.getToken()
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }
}
