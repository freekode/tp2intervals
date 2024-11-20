package org.freekode.tp2intervals.domain.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

interface ActivityRepository {
    fun platform(): Platform

    fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity>

    fun saveActivities(activities: List<Activity>)
}
