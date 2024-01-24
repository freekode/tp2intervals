package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.app.schedule.SchedulerService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.jobrunr.jobs.RecurringJob
import org.jobrunr.scheduling.cron.Cron
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ActivityService(
    private val activityRepositoryStrategy: ActivityRepositoryStrategy,
    private val schedulerService: SchedulerService,
    private val appConfigurationRepository: AppConfigurationRepository,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun syncActivities(syncActivitiesRequest: SyncActivitiesRequest) {
        log.info("Sync activities by request $syncActivitiesRequest")
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

    fun scheduleSyncActivitiesJob(request: SyncActivitiesRequest) {
        val syncActivitiesCron = appConfigurationRepository.getConfiguration("generic.sync-activities-cron")!!
        val jobId = getJobId(request.sourcePlatform, request.targetPlatform)
        schedulerService.startJob(jobId, syncActivitiesCron) { syncActivities(request) }
    }

    fun stopJob(sourcePlatform: Platform, targetPlatform: Platform) {
        schedulerService.stopJob(getJobId(sourcePlatform, targetPlatform))
    }

    fun getJob(sourcePlatform: Platform, targetPlatform: Platform): RecurringJob? {
        return schedulerService.getJob(getJobId(sourcePlatform, targetPlatform))
    }

    private fun getJobId(sourcePlatform: Platform, targetPlatform: Platform): String {
        return "sync-activities-${sourcePlatform}-${targetPlatform}-job"
    }
}
