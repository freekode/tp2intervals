package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsWorkoutRepository
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

    @Ignore
    def "should copy training plan"() {
        given:
        def startDate = LocalDate.parse("2023-12-15")
        def endDate = LocalDate.parse("2023-12-17")

        when:
        mainService.copyPlanFromThirdParty(startDate, endDate)

        then:
        true
    }

    @Ignore
    def "should migrate intervals activities"() {
        given:
        def startDate = LocalDate.parse("2023-12-12")
        def endDate = LocalDate.now()

        when:
        def workouts = intervalsWorkoutRepository.getActivities(startDate, endDate)
        workouts.stream()
                .filter { it.type == TrainingType.WEIGHT || it.type == TrainingType.RUN }
                .forEach { thirdPartyWorkoutRepository.createActivity(it) }

        then:
        workouts != null
    }
}
