package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.domain.Platform
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ActivitySyncJobScheduler {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun startJob(sourcePlatform: Platform, targetPlatform: Platform) {
        log.info("Schedule sync job")
    }

    fun endJob() {
    }

    fun checkJob() {
    }
}
