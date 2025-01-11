package org.freekode.tp2intervals.domain.config

import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.boot.logging.LoggingSystem
import org.springframework.stereotype.Component


@Component
class DebugModeService(
    private val appConfigurationRepository: AppConfigurationRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val generalDebugModeKey = "general.debug-mode"

    init {
        initDebugMode()
    }

    fun handleDebugMode(configMap: Map<String, String?>) {
        if (configMap.contains(generalDebugModeKey) && configMap[generalDebugModeKey]!!.toBoolean()) {
            setLogLevel(LogLevel.DEBUG)
            return
        }
        setLogLevel(LogLevel.INFO)
    }

    private fun initDebugMode() {
        val configurations = appConfigurationRepository.getConfigurations().configMap
        handleDebugMode(configurations)
    }

    private fun setLogLevel(logLevel: LogLevel) {
        val system: LoggingSystem = LoggingSystem.get(this::class.java.getClassLoader())
        system.setLogLevel("org.freekode.tp2intervals", logLevel)
        log.error("Log level set to $logLevel")
    }
}
