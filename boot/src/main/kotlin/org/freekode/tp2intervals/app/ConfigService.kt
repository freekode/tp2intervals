package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.domain.config.AppConfig
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.domain.config.IntervalsConfig
import org.freekode.tp2intervals.domain.config.TrainingPeaksConfig
import org.freekode.tp2intervals.rest.AppConfigDTO
import org.springframework.stereotype.Service

@Service
class ConfigService(
    private val connectionTesters: List<ConnectionTester>,
    private val appConfigRepository: AppConfigRepository
) {

    fun testConnections() = connectionTesters.forEach { it.test() }

    fun getConfig(): AppConfig = appConfigRepository.getConfig()

    fun updateConfig(appConfig: AppConfig) = appConfigRepository.updateConfig(appConfig)
}
