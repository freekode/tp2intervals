package org.freekode.tp2intervals.app.schedule

import org.jobrunr.jobs.RecurringJob
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.scheduling.RecurringJobBuilder
import org.jobrunr.storage.StorageProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SchedulerService(
    private val jobScheduler: JobScheduler,
    private val jobRunrStorageProvider: StorageProvider
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun startJob(jobId: String, cron: String, runnable: Runnable) {
        val jobBuilder = RecurringJobBuilder
            .aRecurringJob()
            .withCron(cron)
            .withName("Sync activities")
            .withId(jobId)
            .withDetails { runnable.run() }
        jobScheduler.createRecurrently(jobBuilder)
        log.info("Schedule sync job")
    }

    fun stopJob(jobId: String) {
        jobScheduler.delete(jobId)
    }

    fun getJob(jobId: String): RecurringJob? {
        return jobRunrStorageProvider.recurringJobs.find { it.id == jobId }
    }
}
