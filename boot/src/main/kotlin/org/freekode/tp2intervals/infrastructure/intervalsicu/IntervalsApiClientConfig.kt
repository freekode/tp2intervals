package org.freekode.tp2intervals.infrastructure.intervalsicu

import feign.RequestInterceptor
import feign.auth.BasicAuthRequestInterceptor
import org.freekode.tp2intervals.infrastructure.config.ConfigurationRepository
import org.springframework.context.annotation.Bean

class IntervalsApiClientConfig(
    private val configurationRepository: ConfigurationRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return BasicAuthRequestInterceptor(
            configurationRepository.getConfiguration().intervalsLogin,
            configurationRepository.getConfiguration().intervalsPassword
        )
    }
}
