package org.freekode.tp2intervals.app.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform

data class PlanWorkoutsRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
) {
    companion object {
        fun fromTodayToTomorrow(sourcePlatform: Platform, targetPlatform: Platform): PlanWorkoutsRequest {
            return PlanWorkoutsRequest(LocalDate.now(), LocalDate.now().plusDays(1), sourcePlatform, targetPlatform)
        }
    }
}
