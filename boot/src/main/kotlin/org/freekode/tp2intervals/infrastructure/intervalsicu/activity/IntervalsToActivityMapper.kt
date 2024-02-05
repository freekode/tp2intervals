package org.freekode.tp2intervals.infrastructure.intervalsicu.activity

import java.time.Duration
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsActivityDTO

class IntervalsToActivityMapper(
    private val eventDTO: IntervalsActivityDTO
) {
    fun mapToActivity(): Activity {
        return Activity(
            eventDTO.start_date_local,
            eventDTO.mapType(),
            eventDTO.name,
            eventDTO.moving_time.let { Duration.ofSeconds(it) },
            eventDTO.icu_training_load,
            null
        )
    }
}
