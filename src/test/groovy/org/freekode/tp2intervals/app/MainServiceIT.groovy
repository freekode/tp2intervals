package org.freekode.tp2intervals.app


import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.IntervalsFolderRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.ThirdPartyWorkoutRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("dev")
class MainServiceIT extends Specification {
    @Autowired
    MainService mainService

    @Autowired
    IntervalsWorkoutRepository intervalsWorkoutRepository

    @Autowired
    ThirdPartyWorkoutRepository thirdPartyWorkoutRepository

    @Autowired
    IntervalsFolderRepository intervalsFolderRepository

    def "should do something"() {
        given:
        def startDate = LocalDate.parse("2023-12-13")
        def endDate = LocalDate.parse("2023-12-14")

        expect:
        def workouts = thirdPartyWorkoutRepository.getWorkouts(startDate, endDate)
        workouts != null
    }
}
