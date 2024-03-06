package org.freekode.tp2intervals.app.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType

data class CopyWorkoutsRequest(
    val name: String,
    val isPlan: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
