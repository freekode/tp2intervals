package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.domain.plan.PlanType
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.springframework.stereotype.Repository

@Repository
class TPPlanRepository(
    private val trainingPeaksPlanApiClient: TrainingPeaksPlanApiClient
) : PlanRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun createPlan(name: String, startDate: LocalDate, type: PlanType): Plan {
        throw PlatformException(platform(), "Doesn't support plan creation")
    }

    override fun getPlans(): List<Plan> {
        val converter = TPPlanConverter()
        return trainingPeaksPlanApiClient.getPlans()
            .map { converter.toPlan(it) }
    }
}
