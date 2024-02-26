package org.freekode.tp2intervals.app


import org.freekode.tp2intervals.app.schedule.ScheduleService
import org.freekode.tp2intervals.app.workout.PlanWorkoutsRequest
import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate

class ScheduleServiceTest extends SpringIT {
    @Autowired
    ScheduleService scheduleService

    def "should do"() {
        given:
        def request = new PlanWorkoutsRequest(LocalDate.now(), LocalDate.now(), [TrainingType.VIRTUAL_BIKE],
                true, Platform.INTERVALS, Platform.TRAINER_ROAD)

        when:
        scheduleService.addScheduledRequest(request)
        def requestS = scheduleService.getScheduledRequest(PlanWorkoutsRequest.class)

        then:
        requestS != null
    }
}
