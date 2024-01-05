package org.freekode.tp2intervals.infrastructure.trainingpeaks

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("training-peaks")
data class TrainingPeaksProperties(
    val authCookie: String
)
