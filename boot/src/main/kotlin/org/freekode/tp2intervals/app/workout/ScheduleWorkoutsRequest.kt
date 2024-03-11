package org.freekode.tp2intervals.app.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType

class ScheduleWorkoutsRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
