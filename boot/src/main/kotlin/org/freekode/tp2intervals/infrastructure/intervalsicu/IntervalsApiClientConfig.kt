package org.freekode.tp2intervals.infrastructure.intervalsicu

import feign.RequestInterceptor
import java.util.*
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class IntervalsApiClientConfig(
    private val appConfigRepository: AppConfigRepository
) {

    companion object {
        private const val LOGIN = "API_KEY"

        fun getAuthorizationHeader(apiKey: String): String {
            val authorization = Base64.getEncoder().encodeToString(("$LOGIN:$apiKey").toByteArray())
            return "Basic $authorization"
        }
    }

    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val apiKey = appConfigRepository.getConfig().intervalsConfig.apiKey
            val authorization = getAuthorizationHeader(apiKey)
            template.header(HttpHeaders.AUTHORIZATION, authorization)
        }
    }

}
