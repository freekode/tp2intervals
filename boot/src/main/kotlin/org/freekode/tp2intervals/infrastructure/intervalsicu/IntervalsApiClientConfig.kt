package org.freekode.tp2intervals.infrastructure.intervalsicu

import feign.RequestInterceptor
import feign.auth.BasicAuthRequestInterceptor
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import java.util.*

class IntervalsApiClientConfig(
    private val appConfigRepository: AppConfigRepository
) {
    private val login = "API_KEY"

    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val apiKey = appConfigRepository.getConfig().intervalsConfig.apiKey
            val authorization = Base64.getEncoder().encodeToString(("$login:$apiKey").toByteArray())
            template.header(HttpHeaders.AUTHORIZATION, "Basic $authorization")
        }
    }
}
