package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.CatchFeignException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class TrainingPeaksConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val trainingPeaksTestApiClient: TrainingPeaksTestApiClient,
    private val cacheManager: CacheManager,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        cacheManager.getCache("platformInfoCache")!!.evict(platform().key)
        cacheManager.getCache("tpAccessTokenCache")?.evict("singleton")
        cacheManager.getCache("tpUserCache")?.evict("singleton")
        cacheManager.getCache("tpWorkoutsCache")?.clear()
        cacheManager.getCache("libraryItemsCache")?.clear()
        val updatedConfig = request.getByPrefix(platform().key)
        if (updatedConfig.isEmpty()) {
            return
        }
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(platform().key)
        val newConfig = currentConfig.configMap + updatedConfig
        validateConfiguration(newConfig, true)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    fun getConfiguration(): TrainingPeaksConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(platform().key)
        return TrainingPeaksConfiguration(config)
    }

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    fun testConnection(authCookie: String): TrainingPeaksConnectionTestResponse {
        if (authCookie.isBlank()) {
            throw PlatformException(Platform.TRAINING_PEAKS, "Access to the platform is not configured")
        }
        val accessToken = trainingPeaksTokenApiClient.getToken(authCookie).accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw PlatformException(Platform.TRAINING_PEAKS, "Access Denied")
        val user = trainingPeaksTestApiClient.getUser("Bearer $accessToken")
        val userMap = user["user"] as? Map<*, *>
        val username = userMap?.get("userName")?.toString()
            ?: userMap?.get("fullName")?.toString()
        val settings = userMap?.get("settings") as? Map<*, *>
        val account = settings?.get("account") as? Map<*, *>
        val isAthlete = account?.get("isAthlete") as? Boolean
        val isPremium = account?.get("isPremium") as? Boolean
        return TrainingPeaksConnectionTestResponse(true, username, isAthlete, isPremium)
    }

    fun isValid(): Boolean {
        try {
            val currentConfig =
                appConfigurationRepository.getConfigurationByPrefix(platform().key)
            validateConfiguration(currentConfig.configMap, false)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun validateConfiguration(newConfig: Map<String, String?>, ignoreEmpty: Boolean) {
        val tpConfig = TrainingPeaksConfiguration(newConfig)
        if (!tpConfig.canValidate() && ignoreEmpty) {
            return
        }
        trainingPeaksTokenApiClient.getToken(tpConfig.authCookie ?: "")
    }
}
