package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksApiTokenRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders


class TrainingPeaksApiClientConfig(
    private val trainingPeaksApiTokenRepository: TrainingPeaksApiTokenRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val token = trainingPeaksApiTokenRepository.getToken()
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }
}
