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
            if (template.headers().keys.any { it.equals(HttpHeaders.AUTHORIZATION, ignoreCase = true) }) return@RequestInterceptor
            val apiKey = intervalsConfigurationRepository.getConfiguration().apiKey
            val authorization = Auth.getAuthorizationHeader(apiKey)
            template.header(HttpHeaders.AUTHORIZATION, authorization)
        }
    }

}
