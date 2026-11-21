package config.mock

import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsCalendarEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO

internal fun IntervalsEventDTO.calendarIdentity() =
    IntervalsCalendarEventDTO(id, description, start_date_local, category, type)
