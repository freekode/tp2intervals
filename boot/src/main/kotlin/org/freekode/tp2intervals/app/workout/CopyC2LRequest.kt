package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToLibraryRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val name: String,
    val isPlan: Boolean,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
