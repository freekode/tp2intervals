package org.freekode.tp2intervals.domain.wellness

import org.freekode.tp2intervals.domain.Platform
import java.time.LocalDate

interface WellnessRepository {
    fun platform(): Platform

    fun getFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Wellness>

    fun saveToCalendar(wellnesses: List<Wellness?>, startDate: LocalDate, endDate: LocalDate)
}
