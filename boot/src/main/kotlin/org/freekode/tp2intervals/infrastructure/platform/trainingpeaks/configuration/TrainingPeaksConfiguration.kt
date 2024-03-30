package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainingPeaksConfiguration(
    val authCookie: String?,
    val planDaysShift: Long,
) {
    companion object {
        private val authCookieKey = "${Platform.TRAINING_PEAKS.key}.auth-cookie"
        private val planDaysShiftKey = "${Platform.TRAINING_PEAKS.key}.copy-plan-days-shift"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String>) : this(
        map[authCookieKey],
        map[planDaysShiftKey]!!.toLong(),
    )

    fun canValidate(): Boolean {
        return authCookie != null && authCookie != "-1"
    }
}
