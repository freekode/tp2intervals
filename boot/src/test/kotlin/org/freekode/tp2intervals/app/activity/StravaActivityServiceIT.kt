package org.freekode.tp2intervals.app.activity

import config.BaseSpringITConfig
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class StravaActivityServiceIT : BaseSpringITConfig() {
    @Autowired
    lateinit var activityService: ActivityService

    @Test
    fun go() {
        activityService.syncActivities(CopyActivitiesRequest(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 4, 1),
            listOf(TrainingType.BIKE),
            Platform.STRAVA,
            Platform.INTERVALS
        ))
    }
}
