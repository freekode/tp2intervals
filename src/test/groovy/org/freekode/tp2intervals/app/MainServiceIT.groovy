package org.freekode.tp2intervals.app


import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.IntervalsFolderRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.ThirdPartyWorkoutRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Ignore
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

    def "should test all connections"() {
        expect:
        mainService.testConnections()
    }

    @Ignore
    def "should do something"() {
        expect:
        mainService.planTodayAndTomorrowWorkouts()
    }
}
