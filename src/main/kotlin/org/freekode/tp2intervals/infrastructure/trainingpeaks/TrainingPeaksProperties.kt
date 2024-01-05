package org.freekode.tp2intervals.infrastructure.trainingpeaks

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("third-party")
data class TrainingPeaksProperties(
    val authCookie: String
)
