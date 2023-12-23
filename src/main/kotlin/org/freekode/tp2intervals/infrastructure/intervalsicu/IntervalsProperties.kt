package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("intervals")
data class IntervalsProperties(
    val login: String,
    val password: String,
    val athleteId: String,
)
