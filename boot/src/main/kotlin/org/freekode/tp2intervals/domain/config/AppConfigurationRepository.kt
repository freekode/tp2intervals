package org.freekode.tp2intervals.domain.config

interface AppConfigurationRepository {
    fun getConfiguration(key: String): AppConfiguration?

    fun getConfigurations(): AppConfiguration

    fun getConfigurationByPrefix(prefix: String): AppConfiguration

    fun updateConfig(request: UpdateConfigurationRequest)
}
