package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ActivityService(
    repositories: List<ActivityRepository>
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val repositoryMap = repositories.associateBy { it.platform() }

    fun syncActivities(request: SyncActivitiesRequest) {
        log.info("Sync activities by request $request")
        val sourceActivityRepository = getRepository(request.sourcePlatform)
        val targetActivityRepository = getRepository(request.targetPlatform)

        val targetActivities = targetActivityRepository.getActivities(request.startDate, request.endDate)
            .filter { request.types.contains(it.type) }

        val sourceActivities = sourceActivityRepository.getActivities(request.startDate, request.endDate)
            .filter { request.types.contains(it.type) }
            .filter { !targetActivities.contains(it) }

        sourceActivities
            .forEach { targetActivityRepository.createActivity(it) }
    }

    private fun getRepository(platform: Platform) = repositoryMap[platform]!!
}
