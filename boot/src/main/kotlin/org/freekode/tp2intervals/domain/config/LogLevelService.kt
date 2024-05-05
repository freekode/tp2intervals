package org.freekode.tp2intervals.domain.config

import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.boot.logging.LoggingSystem
import org.springframework.stereotype.Component

@Component
class LogLevelService(
    private val appConfigurationRepository: AppConfigurationRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    init {
        initLogLevel()
    }

    fun setLogLevel(logLevel: LogLevel) {
        val system: LoggingSystem = LoggingSystem.get(this::class.java.getClassLoader())
        system.setLogLevel("org.freekode.tp2intervals", logLevel)
        log.error("Log level set to $logLevel")
    }

    private fun initLogLevel() {
        val logLevel = appConfigurationRepository.getConfiguration("generic.log-level")
        setLogLevel(LogLevel.valueOf(logLevel!!))
    }
}
