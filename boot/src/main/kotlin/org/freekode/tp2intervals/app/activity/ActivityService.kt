package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.domain.activity.Activity
import org.springframework.stereotype.Service

@Service
class ActivityService(
    private val activityRepositoryStrategy: ActivityRepositoryStrategy,
) {
    fun syncActivities(syncActivitiesRequest: SyncActivitiesRequest) {
        val sourceActivityRepository = activityRepositoryStrategy.getRepository(syncActivitiesRequest.sourcePlatform)
        val targetActivityRepository = activityRepositoryStrategy.getRepository(syncActivitiesRequest.targetPlatform)

        val sourceActivities =
            sourceActivityRepository.getActivities(syncActivitiesRequest.startDate, syncActivitiesRequest.endDate)
        val targetActivities =
            targetActivityRepository.getActivities(syncActivitiesRequest.startDate, syncActivitiesRequest.endDate)

        sourceActivities
            .filter { sourceActivity: Activity -> targetActivities.none { it.isSame(sourceActivity) } }
            .filter { syncActivitiesRequest.types.contains(it.type) }
            .forEach { targetActivityRepository.createActivity(it) }
    }

}
