package org.freekode.tp2intervals.app.schedule

import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration
class ScheduleConfiguration(
    private val appConfigurationRepository: AppConfigurationRepository,
) {

    @Bean
    fun syncActivitiesJobCron(): String {
        return appConfigurationRepository.getConfiguration("generic.sync-activities-cron")!!
    }
}
