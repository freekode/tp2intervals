package org.freekode.tp2intervals.domain.activity

import org.freekode.tp2intervals.domain.Platform
import java.time.LocalDate

@Deprecated("not planned")
interface ActivityRepository {
    fun platform(): Platform

    fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity>

    fun createActivity(activity: Activity)
}
