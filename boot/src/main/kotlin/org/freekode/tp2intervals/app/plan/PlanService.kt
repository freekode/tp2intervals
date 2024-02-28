package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepositoryStrategy
import org.springframework.stereotype.Service

@Service
class PlanService(
    private val planRepositoryStrategy: PlanRepositoryStrategy
) {
    fun getPlans(platform: Platform): List<Plan> {
        val repository = planRepositoryStrategy.getRepository(platform)
        return repository.getPlans()
    }
}
