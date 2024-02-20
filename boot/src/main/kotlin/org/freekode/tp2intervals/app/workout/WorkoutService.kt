package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.schedule.SchedulerService
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.springframework.stereotype.Service

@Service
class WorkoutService(
    private val workoutRepositoryStrategy: WorkoutRepositoryStrategy,
    private val planRepositoryStrategy: PlanRepositoryStrategy,
    private val schedulerService: SchedulerService,
    private val appConfigurationRepository: AppConfigurationRepository
) {
    fun planWorkouts(request: PlanWorkoutsRequest): PlanWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)

        val allWorkouts = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts
            .filter { request.types.contains(it.type) }

        val response = PlanWorkoutsResponse(
            filteredWorkouts.size,
            allWorkouts.size - filteredWorkouts.size,
            request.startDate,
            request.endDate
        )
        filteredWorkouts.forEach { targetWorkoutRepository.planWorkout(it) }
        return response
    }

    fun copyWorkouts(request: CopyWorkoutsRequest): CopyWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)
        val targetPlanRepository = planRepositoryStrategy.getRepository(request.targetPlatform)

        val allWorkouts = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts
            .filter { request.types.contains(it.type) }

        val response = CopyWorkoutsResponse(
            filteredWorkouts.size,
            allWorkouts.size - filteredWorkouts.size,
            request.startDate,
            request.endDate
        )
        val plan = targetPlanRepository.createPlan(request.name, request.startDate, request.planType)
        filteredWorkouts.forEach { targetWorkoutRepository.saveWorkout(it, plan) }
        return response
    }
}
