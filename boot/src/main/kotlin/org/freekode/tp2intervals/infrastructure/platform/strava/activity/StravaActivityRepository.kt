package org.freekode.tp2intervals.infrastructure.platform.strava.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.strava.StravaApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import kotlin.math.ceil

@Repository
class StravaActivityRepository(
    val stravaApiClient: StravaApiClient
) : ActivityRepository {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun platform() = Platform.STRAVA

    override fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity> {
        var activitiesResponse = stravaApiClient.getActivities(1)
        val pages = ceil(activitiesResponse.total.toDouble() / activitiesResponse.perPage).toInt()

        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(23, 59, 59)
        val activities = mutableListOf<Activity>()
        for (i in 2..pages) {
            for (activityDTO in activitiesResponse.models) {
                val activity = StravaActivityMapper().toActivity(activityDTO)

                if (activity.startedAt.isAfter(endDateTime)) {
                    log.warn("End already")
                    break
                }

                if (!activity.startedAt.isBefore(startDateTime) &&
                    !activity.startedAt.isAfter(endDateTime) &&
                    types.contains(activity.type)) {
                    activities.add(activity)
                }
            }
            activitiesResponse = stravaApiClient.getActivities(i)
        }
        return activities
    }

    override fun saveActivities(activities: List<Activity>) {
        TODO("Not yet implemented")
    }
}
