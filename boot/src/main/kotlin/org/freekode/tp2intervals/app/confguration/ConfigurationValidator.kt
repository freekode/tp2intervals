package org.freekode.tp2intervals.app.confguration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration

interface ConfigurationValidator {
    fun platform(): Platform

    fun validate(appConfiguration: AppConfiguration)
}
