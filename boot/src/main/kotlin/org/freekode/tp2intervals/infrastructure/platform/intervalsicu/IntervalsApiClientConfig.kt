package org.freekode.tp2intervals.infrastructure.platform.intervalsicu

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.utils.Auth
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class IntervalsApiClientConfig(
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val apiKey = intervalsConfigurationRepository.getConfiguration().apiKey
            val authorization = Auth.getAuthorizationHeader(apiKey)
            template.header(HttpHeaders.AUTHORIZATION, authorization)
        }
    }

}
