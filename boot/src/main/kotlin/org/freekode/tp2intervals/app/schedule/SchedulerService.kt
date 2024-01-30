package org.freekode.tp2intervals.app.schedule

import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service

@Service
class SchedulerService(
    private val scheduler: TaskScheduler
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun startJob(jobId: String, cron: String, runnable: Runnable) {
    }

    fun stopJob(jobId: String) {
    }

    fun getJob(jobId: String): String {
        return ""
    }
}
