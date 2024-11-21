package org.freekode.tp2intervals.infrastructure.platform.strava.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.strava.StravaApiClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import kotlin.math.ceil

@Repository
class StravaActivityRepository(
    private val stravaApiClientService: StravaApiClientService
) : ActivityRepository {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun platform() = Platform.STRAVA

    override fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity> {
        var activitiesResponse = stravaApiClientService.getActivities(1)
        val pages = ceil(activitiesResponse.total.toDouble() / activitiesResponse.perPage).toInt()

        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(23, 59, 59)
        val activities = mutableListOf<Activity>()
        for (i in 2..pages) {
            for (activityDTO in activitiesResponse.models) {
                var activity = StravaActivityMapper().toActivity(activityDTO)

                if (activity.startedAt.isBefore(startDateTime.minusDays(2))) {
                    log.info("Current date is already 2 days early than search start date, ending search")
                    return activities
                }

                if (!activity.startedAt.isBefore(startDateTime) &&
                    !activity.startedAt.isAfter(endDateTime) &&
                    types.contains(activity.type)
                ) {
                    activity = downloadResourceIfExists(activityDTO.id, activity)
                    activities.add(activity)
                }
            }
            activitiesResponse = stravaApiClientService.getActivities(i)
        }
        return activities
    }

    override fun saveActivities(activities: List<Activity>) {
        TODO("Not yet implemented")
    }

    private fun downloadResourceIfExists(activityId: String, activity: Activity): Activity {
        val resource = stravaApiClientService.exportOriginal(activityId)
        return resource?.let { activity.withResource(it) } ?: activity
    }
}
