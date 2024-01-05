package org.freekode.tp2intervals.domain.config

interface AppConfigRepository {
    fun getConfig(): AppConfig

    fun updateConfig(appConfig: AppConfig)
}
