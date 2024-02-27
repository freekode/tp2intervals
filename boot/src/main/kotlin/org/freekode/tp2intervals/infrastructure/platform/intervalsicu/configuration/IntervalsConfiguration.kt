package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.infrastructure.PlatformException

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

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String>) : this(
        map[apiKeyConfigKey]!!,
        map[athleteIdConfigKey]!!,
        map[powerRangeConfigKey]!!.toFloat(),
        map[hrRangeConfigKey]!!.toFloat(),
        map[paceRangeConfigKey]!!.toFloat(),
    ) {
        val wrongValues = map.entries.filter { it.value == "-1" }
        if (wrongValues.isNotEmpty()) {
            val entriesString = wrongValues.joinToString(separator = ", ") { it.toString() }
            throw PlatformException(Platform.INTERVALS, "Wrong values: $entriesString")
        }
    }
}
