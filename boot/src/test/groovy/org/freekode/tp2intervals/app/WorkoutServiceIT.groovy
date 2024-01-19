package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.app.config.ConfigService
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.IntervalsFolderRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.TrainingPeaksWorkoutRepository
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
    ConfigService configService

    @Autowired
    IntervalsWorkoutRepository intervalsWorkoutRepository

    @Autowired
    TrainingPeaksWorkoutRepository thirdPartyWorkoutRepository

    @Autowired
    IntervalsFolderRepository intervalsFolderRepository

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
