package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder.IntervalsPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Ignore
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("dev")
class WorkoutServiceIT extends Specification {
    @Autowired
    WorkoutService mainService

    @Autowired
    ConfigurationService configService

    @Autowired
    IntervalsWorkoutRepository intervalsWorkoutRepository

    @Autowired
    TrainingPeaksWorkoutRepository thirdPartyWorkoutRepository

    @Autowired
    IntervalsPlanRepository intervalsFolderRepository

    @Ignore
    def "should test all connections"() {
        expect:
        configService.testConnections()
    }

    @Ignore
    def "should do something"() {
        expect:
        mainService.planTodayAndTomorrowWorkouts()
    }
}
