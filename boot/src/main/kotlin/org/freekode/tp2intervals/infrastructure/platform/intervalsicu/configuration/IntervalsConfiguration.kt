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
        private val apiKeyConfigKey = "${Platform.INTERVALS.key}.api-key"
        private val athleteIdConfigKey = "${Platform.INTERVALS.key}.athlete-id"
        private val powerRangeConfigKey = "${Platform.INTERVALS.key}.power-range"
        private val hrRangeConfigKey = "${Platform.INTERVALS.key}.hr-range"
        private val paceRangeConfigKey = "${Platform.INTERVALS.key}.pace-range"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String>) : this(
        map[apiKeyConfigKey]!!,
        map[athleteIdConfigKey]!!,
        map[powerRangeConfigKey]!!.toFloat(),
        map[hrRangeConfigKey]!!.toFloat(),
        map[paceRangeConfigKey]!!.toFloat(),
    ) {
        val wrongValues = map.entries
            .filter { it.value == "-1" || it.value.isBlank() }
        if (wrongValues.isNotEmpty()) {
            val entriesString = wrongValues.joinToString(separator = ", ") { it.toString() }
            throw PlatformException(Platform.INTERVALS, "Wrong values: $entriesString")
        }
    }
}
