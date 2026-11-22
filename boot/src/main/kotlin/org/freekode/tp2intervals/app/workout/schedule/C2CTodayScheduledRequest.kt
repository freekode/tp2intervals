package org.freekode.tp2intervals.app.workout.schedule

import com.fasterxml.jackson.annotation.JsonInclude
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class C2CTodayScheduledRequest(
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    @get:JsonInclude(JsonInclude.Include.NON_NULL) val window: ScheduleWindow? = null,
) : Schedulable {
    fun forToday(today: LocalDate = LocalDate.now()) = CopyFromCalendarToCalendarRequest(
        today,
        if (window == ScheduleWindow.TODAY_AND_TOMORROW) today.plusDays(1) else today,
        types,
        skipSynced,
        sourcePlatform,
        targetPlatform
    )
}

enum class ScheduleWindow { TODAY_AND_TOMORROW }
