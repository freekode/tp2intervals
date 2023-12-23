package org.freekode.tp2intervals.infrastructure.thirdparty

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.thirdparty.token.ThirdPartyApiTokenRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders


class ThirdPartyApiClientConfig(
    private val thirdPartyApiTokenRepository: ThirdPartyApiTokenRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val token = thirdPartyApiTokenRepository.getToken()
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }
}
