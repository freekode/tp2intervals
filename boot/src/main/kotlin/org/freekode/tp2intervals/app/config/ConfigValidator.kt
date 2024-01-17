package org.freekode.tp2intervals.app.config

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfig

interface ConfigValidator {
    fun platform(): Platform

    fun validate(appConfig: AppConfig)
}
