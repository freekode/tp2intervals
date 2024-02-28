package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.Platform
import org.springframework.beans.factory.annotation.Autowired

class PlanServiceTest extends SpringIT {
    @Autowired
    PlanService planService

    def "go"() {
        def plans = planService.getPlans(Platform.TRAINING_PEAKS)

        expect:
        !plans.isEmpty()
    }
}
