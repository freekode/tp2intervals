package org.freekode.tp2intervals.infrastructure.intervalsicu

import feign.RequestInterceptor
import feign.auth.BasicAuthRequestInterceptor
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.context.annotation.Bean

class IntervalsApiClientConfig(
    private val appConfigRepository: AppConfigRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return BasicAuthRequestInterceptor(
            appConfigRepository.getConfig().intervalsConfig.login,
            appConfigRepository.getConfig().intervalsConfig.password
        )
    }
}
