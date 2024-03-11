package org.freekode.tp2intervals.app


import org.freekode.tp2intervals.app.schedule.ScheduleService
import org.freekode.tp2intervals.app.workout.ScheduleWorkoutsRequest
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
        def request = new ScheduleWorkoutsRequest(LocalDate.now(), LocalDate.now(), [TrainingType.VIRTUAL_BIKE],
                true, Platform.INTERVALS, Platform.TRAINER_ROAD)

        when:
        scheduleService.addScheduledRequest(request)
        def newReq = scheduleService.getScheduledRequest(ScheduleWorkoutsRequest.class)

        then:
        newReq != null
    }
}
