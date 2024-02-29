package org.freekode.tp2intervals.app.plan

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepositoryStrategy
import org.freekode.tp2intervals.domain.plan.PlanType
import org.freekode.tp2intervals.domain.workout.WorkoutRepositoryStrategy
import org.springframework.stereotype.Service

@Service
class PlanService(
    private val planRepositoryStrategy: PlanRepositoryStrategy,
    private val workoutRepositoryStrategy: WorkoutRepositoryStrategy,
) {
    fun getPlans(platform: Platform): List<Plan> {
        val repository = planRepositoryStrategy.getRepository(platform)
        return repository.getPlans()
    }

    fun copyPlan(request: CopyPlanRequest): CopyPlanResponse {
        val targetPlanRepository = planRepositoryStrategy.getRepository(request.targetPlatform)
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)

        val workouts = sourceWorkoutRepository.getWorkouts(request.plan)
        val newPlan = targetPlanRepository.createPlan(request.plan.name, LocalDate.now(), PlanType.PLAN)
        workouts.forEach { targetWorkoutRepository.saveWorkout(it, newPlan) }
        return CopyPlanResponse(workouts.size, newPlan.startDate)
    }
}
