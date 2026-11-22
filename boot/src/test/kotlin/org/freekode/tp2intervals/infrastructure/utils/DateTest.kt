package org.freekode.tp2intervals.infrastructure.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DateTest {
    @Test
    fun `calendar date is formatted without timezone`() {
        assertEquals(
            "2026-11-24T00:00:00",
            Date.toIsoLocalDateTime(LocalDate.of(2026, 11, 24)),
        )
    }

    @Test
    fun `calendar date query uses ISO local date`() {
        assertEquals(
            "2026-11-24",
            Date.toIsoLocalDate(LocalDate.of(2026, 11, 24)),
        )
    }

    @Test
    fun `timestamp formatting does not add an offset`() {
        assertEquals(
            "2026-11-24T07:30:15",
            Date.toIsoLocalDateTime(LocalDateTime.of(2026, 11, 24, 7, 30, 15)),
        )
    }

    @Test
    fun `calendar date and default workout time are formatted together`() {
        assertEquals(
            "2026-11-24T18:00:00",
            Date.toIsoLocalDateTime(
                LocalDate.of(2026, 11, 24),
                LocalTime.of(18, 0),
            ),
        )
    }
}
