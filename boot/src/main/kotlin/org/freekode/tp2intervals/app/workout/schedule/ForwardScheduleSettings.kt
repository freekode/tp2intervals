package org.freekode.tp2intervals.app.workout.schedule

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Component
class ForwardScheduleSettings(
    @Value("\${app.training-peaks.schedule-interval-minutes:60}") val intervalMinutes: Long = 60,
    @Value("\${app.training-peaks.schedule-timezone:}") timezone: String = "",
    private val clock: Clock = Clock.systemUTC(),
) {
    private val zones = ZoneId.getAvailableZoneIds()
    val timezone: String = timezone.ifBlank { ZoneId.systemDefault().id.takeIf(zones::contains) ?: "UTC" }

    init {
        require(intervalMinutes in 5..1440) { "Forward schedule interval must be between 5 and 1440 minutes" }
        require(this.timezone in zones) { "Forward calendar timezone must be an IANA timezone" }
    }

    private val zone: ZoneId = ZoneId.of(this.timezone)

    fun now(): Instant = clock.instant()

    fun today(now: Instant = now()): LocalDate = now.atZone(zone).toLocalDate()
}
