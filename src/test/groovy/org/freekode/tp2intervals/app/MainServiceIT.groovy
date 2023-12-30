package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.domain.TrainingType
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

    @Ignore
    def "should copy training plan"() {
        given:
        def startDate = LocalDate.parse("2023-12-12")
        def endDate = LocalDate.parse("2023-12-15")

        expect:
        def workouts = thirdPartyWorkoutRepository.getWorkouts(startDate, endDate)
        workouts != null
        def plan = intervalsFolderRepository.createPlan("My Plan - $startDate", startDate)
        workouts.forEach { intervalsWorkoutRepository.createAndPlanWorkout(plan, it) }
    }

    @Ignore
    def "should migrate intervals activities"() {
        given:
        def startDate = LocalDate.parse("2023-12-12")
        def endDate = LocalDate.now()

        expect:
        def workouts = intervalsWorkoutRepository.getActivities(startDate, endDate)
        workouts.stream()
                .filter { it.type == TrainingType.WEIGHT || it.type == TrainingType.RUN }
                .forEach { thirdPartyWorkoutRepository.createActivity(it) }
    }

    def "should migrate intervals workouts"() {
        given:
        def startDate = LocalDate.now()
        def endDate = LocalDate.now().plusDays(1)

        expect:
        def workouts = intervalsWorkoutRepository.getPlannedWorkouts(startDate, endDate)
        workouts != null
        workouts.forEach { thirdPartyWorkoutRepository.planWorkout(it) }
    }

}
