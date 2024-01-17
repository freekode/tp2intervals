package org.freekode.tp2intervals.app.activity

import java.time.LocalDate
import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.stereotype.Service

@Service
class ActivityService(
    private val activityRepositoryStrategy: ActivityRepositoryStrategy,
) {
    fun syncActivities(syncActivitiesRequest: SyncActivitiesRequest) {
        val fromActivityRepository = activityRepositoryStrategy.getRepository(syncActivitiesRequest.fromPlatform)
        val toActivityRepository = activityRepositoryStrategy.getRepository(syncActivitiesRequest.toPlatform)

        val workouts = fromActivityRepository.getActivities(syncActivitiesRequest.startDate, syncActivitiesRequest.endDate)
        workouts
            .filter { syncActivitiesRequest.types.contains(it.type) }
            .forEach { toActivityRepository.createActivity(it) }
    }

}
