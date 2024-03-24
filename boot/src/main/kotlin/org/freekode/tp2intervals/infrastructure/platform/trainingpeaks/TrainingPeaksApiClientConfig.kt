package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks

import feign.RequestInterceptor
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders


class TrainingPeaksApiClientConfig(
    private val trainingPeaksTokenRepository: TrainingPeaksTokenRepository
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val token = trainingPeaksTokenRepository.getToken()
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }
}
