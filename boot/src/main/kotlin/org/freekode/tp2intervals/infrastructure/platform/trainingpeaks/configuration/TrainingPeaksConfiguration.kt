package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.config.AppConfiguration

data class TrainingPeaksConfiguration(
    val authCookie: String?,
    val planDaysShift: Long,
) {
    companion object {
        const val CONFIG_PREFIX = "training-peaks"
        private const val authCookieKey = "${CONFIG_PREFIX}.auth-cookie"
        private const val planDaysShiftKey = "${CONFIG_PREFIX}.copy-plan-days-shift"
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
