package org.freekode.tp2intervals.app.workout.scheduled

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToCalendarScheduledRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
) : Schedulable {
    fun forToday() = CopyFromCalendarToCalendarScheduledRequest(
        LocalDate.now(),
        LocalDate.now(),
        types,
        skipSynced,
        sourcePlatform,
        targetPlatform
    )
}
