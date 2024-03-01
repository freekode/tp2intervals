package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.springframework.stereotype.Repository

@Repository
class TPPlanRepository(
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val trainingPeaksPlanApiClient: TrainingPeaksPlanApiClient
) : PlanRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun createPlan(name: String, startDate: LocalDate, isPlan: Boolean): Plan {
        throw PlatformException(platform(), "Doesn't support plan creation")
    }

    override fun getPlans(): List<Plan> {
        val converter = TPPlanConverter()
        return trainingPeaksPlanApiClient.getPlans()
            .map { converter.toPlan(it) }
    }

    fun getPlan(planId: String): TPPlanDto {
        return trainingPeaksPlanApiClient.getPlan(planId)
    }

    fun applyPlan(planId: String, startDate: LocalDate): ApplyTPPlanResponseDTO {
        val request = ApplyTPPlanRequestDTO(
            trainingPeaksUserRepository.getUserId(),
            planId,
            startDate.toString(),
            "1"
        )
        return trainingPeaksPlanApiClient.applyPlan(listOf(request)).first()
    }

    fun removeAppliedPlan(appliedPlanId: String) {
        val request = mapOf(
            "appliedPlanId" to appliedPlanId
        )
        trainingPeaksPlanApiClient.removePlan(request)
    }
}
