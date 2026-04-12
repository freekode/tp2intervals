package org.freekode.tp2intervals.infrastructure.utils

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.time.DayOfWeek
import java.time.LocalDate

class DateTest {

    @Test
    fun `should calculate days difference between two dates`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 1, 11)

        val result = Date.daysDiff(start, end)

        expectThat(result).isEqualTo(10)
    }

    @Test
    fun `should calculate days difference regardless of order`() {
        val start = LocalDate.of(2024, 1, 11)
        val end = LocalDate.of(2024, 1, 1)

        val result = Date.daysDiff(start, end)

        expectThat(result).isEqualTo(10)
    }

    @Test
    fun `should return zero for same date`() {
        val date = LocalDate.of(2024, 1, 1)

        val result = Date.daysDiff(date, date)

        expectThat(result).isEqualTo(0)
    }

    @Test
    fun `should return this monday for any day of week`() {
        val monday = Date.thisMonday()

        expectThat(monday.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `should generate dates between start and end inclusive`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 1, 5)

        val result = Date.getDatesBetween(start, end)

        expectThat(result).hasSize(5)
        expectThat(result.first()).isEqualTo(LocalDate.of(2024, 1, 1))
        expectThat(result.last()).isEqualTo(LocalDate.of(2024, 1, 5))
    }

    @Test
    fun `should return single date when start equals end`() {
        val date = LocalDate.of(2024, 1, 1)

        val result = Date.getDatesBetween(date, date)

        expectThat(result).hasSize(1)
        expectThat(result.first()).isEqualTo(date)
    }

    @Test
    fun `should return empty list when start is after end`() {
        val start = LocalDate.of(2024, 1, 5)
        val end = LocalDate.of(2024, 1, 1)

        val result = Date.getDatesBetween(start, end)

        expectThat(result).isEmpty()
    }
}
