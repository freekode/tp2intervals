package org.freekode.tp2intervals.app.activity

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ActivityService(
    private val activityRepositoryStrategy: ActivityRepositoryStrategy,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun syncActivities(request: SyncActivitiesRequest) {
        log.info("Sync activities by request $request")
        val sourceActivityRepository = activityRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetActivityRepository = activityRepositoryStrategy.getRepository(request.targetPlatform)

        val targetActivities = targetActivityRepository.getActivities(request.startDate, request.endDate)
            .filter { request.types.contains(it.type) }

        val sourceActivities = sourceActivityRepository.getActivities(request.startDate, request.endDate)
            .filter { request.types.contains(it.type) }
            .filter { !targetActivities.contains(it) }

        sourceActivities
            .forEach { targetActivityRepository.createActivity(it) }
    }
}
