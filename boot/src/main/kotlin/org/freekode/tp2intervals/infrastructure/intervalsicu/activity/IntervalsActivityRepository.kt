package org.freekode.tp2intervals.infrastructure.intervalsicu.activity

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsToWorkoutMapper
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.WorkoutToIntervalsMapper
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class IntervalsActivityRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val appConfigRepository: AppConfigRepository,
    private val intervalsToWorkoutMapper: IntervalsToWorkoutMapper,
) : ActivityRepository {
    override fun platform() = Platform.INTERVALS

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val activities =
            intervalsApiClient.getActivities(
                appConfigRepository.getConfig().intervalsConfig.athleteId,
                startDate.toString(),
                endDate.toString()
            )
        return activities
            .map { intervalsToWorkoutMapper.mapToActivity(it) }
    }

    override fun createActivity(activity: Activity) {
        println(activity)
//        intervalsApiClient.createActivity(
//            appConfigRepository.getConfig().intervalsConfig.athleteId,
//            CreateActivityRequestDTO(activity.resource!!)
//        )
    }
}
