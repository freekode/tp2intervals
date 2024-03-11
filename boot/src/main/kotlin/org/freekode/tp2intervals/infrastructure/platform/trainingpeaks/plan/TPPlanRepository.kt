package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["libraryItemsCache"])
@Repository
class TPPlanRepository(
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val trainingPeaksPlanApiClient: TrainingPeaksPlanApiClient,
    private val tpWorkoutLibraryRepository: TPWorkoutLibraryRepository,
) : PlanRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun createPlan(name: String, startDate: LocalDate, isPlan: Boolean): Plan {
        throw PlatformException(platform(), "Doesn't support plan creation")
    }

    @Cacheable(key = "'TRAINING_PEAKS'")
    override fun getLibraries(): List<Plan> {
        val plans = trainingPeaksPlanApiClient.getPlans()
            .map { toPlan(it) }
            .sortedBy { it.name }
        val libraries = tpWorkoutLibraryRepository.getLibraries()
            .sortedBy { it.name }
        return (plans + libraries)
            .toList()
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

    private fun toPlan(planDto: TPPlanDto): Plan {
        return Plan.planFromMonday(
            planDto.title,
            ExternalData.empty().withTrainingPeaks(planDto.planId)
        )
    }
}
