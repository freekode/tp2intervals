package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest

data class IntervalsConfiguration(
    val apiKey: String,
    val athleteId: String,
    val powerRange: Float,
    val hrRange: Float,
    val paceRange: Float,
) {
    companion object {
        const val CONFIG_PREFIX = "intervals"

        private const val apiKeyConfigKey = "${CONFIG_PREFIX}.api-key"
        private const val athleteIdConfigKey = "${CONFIG_PREFIX}.athlete-id"
        private const val powerRangeConfigKey = "${CONFIG_PREFIX}.power-range"
        private const val hrRangeConfigKey = "${CONFIG_PREFIX}.hr-range"
        private const val paceRangeConfigKey = "${CONFIG_PREFIX}.pace-range"
    }

    constructor(appConfiguration: AppConfiguration) : this(
        appConfiguration.get(apiKeyConfigKey),
        appConfiguration.get(athleteIdConfigKey),
        appConfiguration.get(powerRangeConfigKey).toFloat(),
        appConfiguration.get(hrRangeConfigKey).toFloat(),
        appConfiguration.get(paceRangeConfigKey).toFloat(),
    )

    fun merge(newConfig: Map<String, String>): IntervalsConfiguration {
        return IntervalsConfiguration(
            newConfig[apiKeyConfigKey] ?: apiKey,
            newConfig[athleteIdConfigKey] ?: athleteId,
            newConfig[powerRangeConfigKey]?.toFloat() ?: powerRange,
            newConfig[hrRangeConfigKey]?.toFloat() ?: hrRange,
            newConfig[paceRangeConfigKey]?.toFloat() ?: paceRange,
        )
    }

    fun getUpdateRequest(): UpdateConfigurationRequest {
        return UpdateConfigurationRequest(
            mapOf(
                apiKeyConfigKey to apiKey,
                athleteIdConfigKey to athleteId,
                powerRangeConfigKey to powerRange.toString(),
                hrRangeConfigKey to hrRange.toString(),
                paceRangeConfigKey to paceRange.toString(),
            )
        )
    }
}
