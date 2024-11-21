package org.freekode.tp2intervals.infrastructure.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.absoluteValue

class Date {
    companion object {
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
