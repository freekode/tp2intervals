package org.freekode.tp2intervals.infrastructure.dev

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dev")
class DevConfiguration(
    val config: Map<String, String>,
)
