package org.freekode.tp2intervals.app.confguration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class DefaultConfigurationProperties(
    val defaultConfigurations: List<Value>
) {
    class Value(
        val key: String,
        val value: String
    )
}
