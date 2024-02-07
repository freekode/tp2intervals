package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.app.schedule.SchedulerService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ActivityService(
    private val activityRepositoryStrategy: ActivityRepositoryStrategy,
    private val schedulerService: SchedulerService,
    private val appConfigurationRepository: AppConfigurationRepository,
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

    fun scheduleSyncActivitiesJob(request: SyncActivitiesRequest) {
        val syncActivitiesCron = appConfigurationRepository.getConfiguration("generic.sync-activities-cron")!!
        val jobId = getJobId(request.sourcePlatform, request.targetPlatform)
        schedulerService.startJob(jobId, syncActivitiesCron) { syncActivities(request) }
    }

    fun stopJob(sourcePlatform: Platform, targetPlatform: Platform) {
        schedulerService.stopJob(getJobId(sourcePlatform, targetPlatform))
    }

    fun getJob(sourcePlatform: Platform, targetPlatform: Platform): String {
        return schedulerService.getJob(getJobId(sourcePlatform, targetPlatform))
    }

    private fun getJobId(sourcePlatform: Platform, targetPlatform: Platform): String {
        return "sync-activities-${sourcePlatform}-${targetPlatform}-job"
    }
}
