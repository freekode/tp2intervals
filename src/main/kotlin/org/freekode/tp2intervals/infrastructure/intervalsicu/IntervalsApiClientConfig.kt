package org.freekode.tp2intervals.infrastructure.intervalsicu

import feign.RequestInterceptor
import feign.auth.BasicAuthRequestInterceptor
import org.springframework.context.annotation.Bean

class IntervalsApiClientConfig(
    private val intervalsProperties: IntervalsProperties
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return BasicAuthRequestInterceptor(intervalsProperties.login, intervalsProperties.password)
    }
}
