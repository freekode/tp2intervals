package org.freekode.tp2intervals.infrastructure.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.absoluteValue

class Date {
    companion object {
        private val isoLocalDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        fun toIsoLocalDate(date: LocalDate): String =
            date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

        fun toIsoLocalDateTime(date: LocalDate): String =
            date.atStartOfDay().format(isoLocalDateTimeFormatter)

        fun toIsoLocalDateTime(date: LocalDate, time: LocalTime): String =
            date.atTime(time).format(isoLocalDateTimeFormatter)

        fun toIsoLocalDateTime(dateTime: LocalDateTime): String =
            dateTime.format(isoLocalDateTimeFormatter)

        fun daysDiff(startDate: LocalDate, endDate: LocalDate): Int {
            return ChronoUnit.DAYS.between(startDate, endDate).toInt().absoluteValue
        }

        fun thisMonday(): LocalDate {
            return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        fun getDatesBetween(startDate: LocalDate, endDate: LocalDate) =
            generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { !it.isAfter(endDate) }
                .toList()
    }
}
