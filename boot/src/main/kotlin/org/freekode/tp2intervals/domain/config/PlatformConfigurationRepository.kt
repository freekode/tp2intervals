package org.freekode.tp2intervals.domain.config

import org.freekode.tp2intervals.domain.Platform

interface PlatformConfigurationRepository {
    fun platform(): Platform

    fun updateConfig(request: UpdateConfigurationRequest)

    fun isValid(): Boolean
}
