package org.freekode.tp2intervals.infrastructure.intervalsicu

import feign.RequestInterceptor
import feign.auth.BasicAuthRequestInterceptor
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.context.annotation.Bean

class IntervalsApiClientConfig(
    private val appConfigRepository: AppConfigRepository
) {
    private val login = "API_KEY"

    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return BasicAuthRequestInterceptor(login, appConfigRepository.getConfig().intervalsConfig.apiKey)
    }
}
