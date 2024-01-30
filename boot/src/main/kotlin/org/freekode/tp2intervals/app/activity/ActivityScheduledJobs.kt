package org.freekode.tp2intervals.app.activity

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleCrudRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class ActivityScheduledJobs(
    private val activityService: ActivityService,
    private val scheduleCrudRepository: ScheduleCrudRepository,
    private val objectMapper: ObjectMapper
) {

    @Scheduled(cron = "#{syncActivitiesJobCron}")
    fun syncActivitiesJob() {
        scheduleCrudRepository
    }
}
