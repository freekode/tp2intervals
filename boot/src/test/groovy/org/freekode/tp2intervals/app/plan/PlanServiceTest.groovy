package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.config.SpringIT
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TPPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.springframework.beans.factory.annotation.Autowired

class PlanServiceTest extends SpringIT {
    @Autowired
    PlanService planService

    @Autowired
    TPPlanRepository tpPlanRepository

    @Autowired
    TrainingPeaksWorkoutRepository trainingPeaksWorkoutRepository

    def "should get plans"() {
        def plans = planService.getPlans(Platform.TRAINING_PEAKS)

        expect:
        !plans.isEmpty()
    }

    def "should copy plan"() {
        given:
        def plans = planService.getPlans(Platform.TRAINING_PEAKS)
        def plan = plans.stream()
                .filter { it.externalData.trainingPeaksId == "124295" }
                .findFirst()
                .orElseThrow()
        when:
        def response = planService.copyPlan(new CopyPlanRequest(plan, Platform.TRAINING_PEAKS, Platform.INTERVALS))

        then:
        response != null
    }

}
