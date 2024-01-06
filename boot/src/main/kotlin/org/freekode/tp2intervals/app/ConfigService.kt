package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.springframework.stereotype.Service

@Service
class ConfigService(
    private val connectionTesters: List<ConnectionTester>,
    private val appConfigRepository: AppConfigRepository
) {

    fun testConnections(): List<String> {
        return connectionTesters
            .map {
                try {
                    it.test()
                    ""
                } catch (e: Exception) {
                    e.message!!
                }
            }
            .filter { it.isNotBlank() }
    }

    fun findConfig(): AppConfig? = appConfigRepository.findConfig()

    fun updateConfig(appConfig: AppConfig) = appConfigRepository.updateConfig(appConfig)
}
