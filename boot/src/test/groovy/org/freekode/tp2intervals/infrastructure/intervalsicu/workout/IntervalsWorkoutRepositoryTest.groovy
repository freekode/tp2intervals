package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.config.ISpringConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("dev")
class IntervalsWorkoutRepositoryTest extends ISpringConfiguration {
    @Autowired
    IntervalsWorkoutRepository intervalsWorkoutRepository

    def "should parse and map planned workouts"() {
        given:
        true

        when:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(LocalDate.now(), LocalDate.now())

        then:
        workouts != null
    }
}
