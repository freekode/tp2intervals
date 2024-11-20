package org.freekode.tp2intervals.infrastructure.platform.strava

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.platform.strava.configuration.StravaConfigurationRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders


class StravaApiClientConfig(
    private val stravaConfigurationRepository: StravaConfigurationRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = stravaConfigurationRepository.getConfiguration().cookie
            template.header(HttpHeaders.COOKIE, cookie)
            template.header("X-Requested-With", "XMLHttpRequest")
        }
    }
}
