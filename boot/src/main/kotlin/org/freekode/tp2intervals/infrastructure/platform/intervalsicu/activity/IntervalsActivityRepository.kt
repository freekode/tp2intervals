package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.activity

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.utils.MyMultipartFile
import org.springframework.stereotype.Repository

@Repository
class IntervalsActivityRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository
) : ActivityRepository {
    override fun platform() = Platform.INTERVALS

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val activities =
            intervalsApiClient.getActivities(
                intervalsConfigurationRepository.getConfiguration().athleteId,
                startDate.atStartOfDay().toString(),
                endDate.atStartOfDay().plusDays(1).minusSeconds(1).toString()
            )
        return activities
            .map { IntervalsToActivityMapper(it).mapToActivity() }
    }

    override fun createActivity(activity: Activity) {
        intervalsApiClient.createActivity(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            MyMultipartFile("file", activity.resource!!)
        )
    }
}
