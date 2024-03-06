package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.plan.Plan

class TPPlanConverter {
    fun toPlan(planDto: TPPlanDto): Plan {
        return Plan.fromMonday(
            planDto.title,
            ExternalData.empty().withTrainingPeaks(planDto.planId)
        )
    }
}
