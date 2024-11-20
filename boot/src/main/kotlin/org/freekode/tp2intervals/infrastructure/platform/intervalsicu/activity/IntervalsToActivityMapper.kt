package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.activity

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsActivityDTO

class IntervalsToActivityMapper(
    private val eventDTO: IntervalsActivityDTO
) {
    fun mapToActivity(): Activity {
        return Activity(
            eventDTO.start_date_local,
            eventDTO.mapType(),
            eventDTO.name,
            null
        )
    }
}
