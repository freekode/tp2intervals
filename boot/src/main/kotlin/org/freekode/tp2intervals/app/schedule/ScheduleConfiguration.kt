package org.freekode.tp2intervals.app.schedule

import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration
class ScheduleConfiguration(
    private val configurationService: ConfigurationService,
) {

    @Bean
    fun planWorkoutsCron(): String {
        return configurationService.getConfiguration("generic.plan-workouts-cron")!!
    }
}
