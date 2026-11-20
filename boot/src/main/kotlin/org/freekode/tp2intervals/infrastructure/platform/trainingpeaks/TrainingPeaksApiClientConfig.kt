package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks

import feign.RequestInterceptor
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import java.security.MessageDigest
import java.util.HexFormat


class TrainingPeaksApiClientConfig(
    private val trainingPeaksTokenRepository: TrainingPeaksTokenRepository,
    private val configuration: TrainingPeaksConfigurationRepository,
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = configuration.getConfiguration().authCookie
                ?: throw PlatformException(Platform.TRAINING_PEAKS, "Access to the platform is not configured")
            val key = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(cookie.toByteArray()))
            val token = trainingPeaksTokenRepository.getToken(cookie, key)
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }
}
