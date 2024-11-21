package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ActivityService(
    repositories: List<ActivityRepository>
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val repositoryMap = repositories.associateBy { it.platform() }

    fun syncActivities(request: CopyActivitiesRequest): CopyActivitiesResponse {
        log.info("Sync activities by request $request")
        val sourceActivityRepository = getRepository(request.sourcePlatform)
        val targetActivityRepository = getRepository(request.targetPlatform)

        val sourceActivities = sourceActivityRepository.getActivities(request.startDate, request.endDate, request.types)
        val activitiesToSave = mutableListOf<Activity>()
        var filteredOut = 0
        for (activity in sourceActivities) {
            if (activity.resource == null ) {
                filteredOut++
                continue
            }
            activitiesToSave.add(activity)
        }
        targetActivityRepository.saveActivities(activitiesToSave)

        return CopyActivitiesResponse(activitiesToSave.size, filteredOut, request.startDate, request.endDate)
    }

    private fun getRepository(platform: Platform) = repositoryMap[platform]!!
}
