package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout


import org.freekode.tp2intervals.config.ISpringConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate

class IntervalsWorkoutRepositoryIT extends ISpringConfiguration {
    @Autowired
    IntervalsWorkoutRepository intervalsWorkoutRepository

    @Autowired
    AppConfigurationRepository appConfigurationRepository

    def setup() {
        appConfigurationRepository.updateConfig(new UpdateConfigurationRequest(
                Map.of(
                        "intervals.api-key", "my-key",
                        "intervals.athlete-id", "my-athlete"
                )
        ))
    }

    def "should parse and convert workouts"() {
        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts.size() == 4
    }
}
