package org.freekode.tp2intervals.app.activity

import config.BaseSpringITConfig
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

@Disabled
class StravaActivityServiceIT : BaseSpringITConfig() {
    @Autowired
    lateinit var activityService: ActivityService

    @Test
    fun go() {
        activityService.syncActivities(CopyActivitiesRequest(
            LocalDate.of(2024, 8, 20),
            LocalDate.of(2024, 8, 30),
            listOf(TrainingType.BIKE),
            Platform.STRAVA,
            Platform.INTERVALS
        ))
    }
}
