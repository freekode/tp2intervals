package org.freekode.tp2intervals.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
class DefaultConfiguration(
    val defaultConfig: Map<String, String>?,
)
