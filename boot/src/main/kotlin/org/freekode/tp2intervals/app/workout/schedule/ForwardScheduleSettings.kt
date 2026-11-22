package org.freekode.tp2intervals.app.workout.schedule

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class ForwardScheduleSettings(
    @Value("\${app.training-peaks.schedule-interval-minutes:60}") val intervalMinutes: Long = 60,
    @Value("\${app.training-peaks.schedule-timezone:Europe/Amsterdam}") val timezone: String = "Europe/Amsterdam",
) {
    init {
        require(intervalMinutes in 5..1440) { "Forward schedule interval must be between 5 and 1440 minutes" }
        require(timezone in ZoneId.getAvailableZoneIds()) { "Forward schedule timezone must be an IANA timezone" }
    }

    val zone: ZoneId = ZoneId.of(timezone)
}
