package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.springframework.stereotype.Service

@Service
class PlanService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<PlanRepository>,
) {
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun getPlans(platform: Platform): List<Plan> {
        val repository = planRepositoryMap[platform]!!
        return repository.getPlans()
    }

    fun copyPlan(request: CopyPlanRequest): CopyPlanResponse {
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workouts = sourceWorkoutRepository.getWorkouts(request.plan)
        val newPlan = targetPlanRepository.createPlan(request.plan.name, Date.thisMonday(), true)
        workouts.forEach { targetWorkoutRepository.saveWorkoutToPlan(it, newPlan) }
        return CopyPlanResponse(newPlan.name, workouts.size)
    }
}
