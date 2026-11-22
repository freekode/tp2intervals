package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.freekode.tp2intervals.infrastructure.utils.MyMultipartFile
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class IntervalsActivityRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository
) : ActivityRepository {
    override fun platform() = Platform.INTERVALS

    override fun saveActivities(activities: List<Activity>) {
        activities.forEach { activity ->
            intervalsApiClient.createActivity(
                intervalsConfigurationRepository.getConfiguration().athleteId,
                activity.title,
                MyMultipartFile("file", activity.resource!!)
            )
        }
    }

    override fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity> {
        val activities =
            intervalsApiClient.getActivities(
                intervalsConfigurationRepository.getConfiguration().athleteId,
                Date.toIsoLocalDateTime(startDate),
                endDate.plusDays(1).atStartOfDay().minusSeconds(1).let(Date::toIsoLocalDateTime)
            )
        return activities
            .map { IntervalsToActivityMapper(it).mapToActivity() }
    }
}
