package org.freekode.tp2intervals.app.confguration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.LogLevelService
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformInfo
import org.freekode.tp2intervals.domain.config.PlatformInfoRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Service

@Service
class ConfigurationService(
    private val platformConfigurationRepositories: List<PlatformConfigurationRepository>,
    platformInfoRepositories: List<PlatformInfoRepository>,
    private val appConfigurationRepository: AppConfigurationRepository,
    private val logLevelService: LogLevelService,
) {
    private val platformInfoRepositoryMap = platformInfoRepositories.associateBy { it.platform() }

    fun getConfiguration(key: String): String? = appConfigurationRepository.getConfiguration(key)

    fun getConfigurations(): AppConfiguration = appConfigurationRepository.getConfigurations()

    fun updateConfiguration(request: UpdateConfigurationRequest): List<String> {
        val errors = platformConfigurationRepositories.mapNotNull { updateConfiguration(request, it) }
        if (request.configMap.contains("generic.log-level")) {
            logLevelService.setLogLevel(LogLevel.valueOf(request.configMap["generic.log-level"]!!))
        }
        return errors
    }

    fun platformInfo(platform: Platform): PlatformInfo {
        return platformInfoRepositoryMap[platform]!!.platformInfo()
    }

    private fun updateConfiguration(
        request: UpdateConfigurationRequest,
        repository: PlatformConfigurationRepository
    ): String? {
        return try {
            repository.updateConfig(request)
            null
        } catch (e: PlatformException) {
            "${e.platform.title}: ${e.message}"
        } catch (e: Exception) {
            e.message
        }
    }
}
