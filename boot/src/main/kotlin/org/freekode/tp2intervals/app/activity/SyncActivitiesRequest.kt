package org.freekode.tp2intervals.app.activity

import java.time.LocalDate
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.TrainingType

data class SyncActivitiesRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val fromPlatform: Platform,
    val toPlatform: Platform
)
