package org.freekode.tp2intervals.domain.plan

import org.freekode.tp2intervals.domain.Platform
import java.time.LocalDate

interface LibraryRepository {
    fun platform(): Platform

    fun createPlan(name: String, startDate: LocalDate, isPlan: Boolean): Plan

    fun getLibraryItems(): List<Plan>
}
