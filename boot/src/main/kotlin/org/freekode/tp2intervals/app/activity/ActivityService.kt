package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ActivityService(
    private val activityRepositoryStrategy: ActivityRepositoryStrategy,
) {
    fun syncActivities(date: LocalDate, types: List<TrainingType>, fromPlatform: Platform, toPlatform: Platform) {
        val fromActivityRepository = activityRepositoryStrategy.getRepository(fromPlatform)
        val toActivityRepository = activityRepositoryStrategy.getRepository(toPlatform)

        val workouts = fromActivityRepository.getActivities(date, date)
        workouts
            .filter { types.contains(it.type) }
            .forEach { toActivityRepository.createActivity(it) }
    }

}
