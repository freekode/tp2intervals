package org.freekode.tp2intervals.app.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform

data class CopyPlanRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
