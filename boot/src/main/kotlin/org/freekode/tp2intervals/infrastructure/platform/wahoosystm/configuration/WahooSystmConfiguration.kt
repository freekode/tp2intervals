package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

import org.freekode.tp2intervals.domain.Platform

data class WahooSystmConfiguration(
    val username: String?,
    val password: String?,
) {
    companion object {
        private val username = "${Platform.WAHOO_SYSTM.key}.username"
        private val password = "${Platform.WAHOO_SYSTM.key}.password"
    }

    constructor(map: Map<String, String>) : this(
        map[WahooSystmConfiguration.username],
        map[WahooSystmConfiguration.password],
    )

    fun canValidate(): Boolean {
        return (username != null && username != "-1") || (password != null && password != "-1")
    }
}
