package org.freekode.tp2intervals.domain.config

interface AppConfigRepository {
    fun getConfig(): AppConfig

    fun findConfig(): AppConfig?

    fun updateConfig(appConfig: AppConfig)
}
