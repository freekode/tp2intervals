package org.freekode.tp2intervals.app

import feign.FeignException
import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ConfigService(
    private val connectionTesters: List<ConnectionTester>,
    private val appConfigRepository: AppConfigRepository
) {

    val log = LoggerFactory.getLogger(this::class.java)

    fun testConnections(): List<String> {
        return connectionTesters.mapNotNull { testConnection(it) }
    }

    fun findConfig(): AppConfig? = appConfigRepository.findConfig()

    fun updateConfig(appConfig: AppConfig) = appConfigRepository.updateConfig(appConfig)

    private fun testConnection(it: ConnectionTester): String? {
        try {
            it.test()
            return null
        } catch (e: FeignException) {
            if (e.status() == HttpStatus.FORBIDDEN.value() || e.status() == HttpStatus.UNAUTHORIZED.value()) {
                return "${it.name()}: access denied"
            }
            log.info("Error during connection check", e)
            return "${it.name()}: error"
        }
    }
}
