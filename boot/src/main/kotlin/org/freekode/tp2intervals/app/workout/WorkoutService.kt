package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.schedule.ScheduleService
import org.freekode.tp2intervals.domain.workout.PlanRepositoryStrategy
import org.freekode.tp2intervals.domain.workout.WorkoutRepositoryStrategy
import org.springframework.stereotype.Service

@Service
class WorkoutService(
    private val workoutRepositoryStrategy: WorkoutRepositoryStrategy,
    private val planRepositoryStrategy: PlanRepositoryStrategy,
    private val scheduleService: ScheduleService
) {
    fun planWorkouts(request: PlanWorkoutsRequest): PlanWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)

        val allWorkoutsToPlan = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        var filteredWorkoutsToPlan = allWorkoutsToPlan
            .filter { request.types.contains(it.type) }
        if (request.skipSynced) {
            val plannedWorkouts = targetWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
                .filter { request.types.contains(it.type) }

            filteredWorkoutsToPlan = filteredWorkoutsToPlan
                .filter { !plannedWorkouts.contains(it) }
        }

        val response = PlanWorkoutsResponse(
            filteredWorkoutsToPlan.size,
            allWorkoutsToPlan.size - filteredWorkoutsToPlan.size,
            request.startDate,
            request.endDate
        )
        filteredWorkoutsToPlan.forEach { targetWorkoutRepository.planWorkout(it) }
        return response
    }

    fun addScheduledPlanWorkoutsRequest(request: PlanWorkoutsRequest) {
        scheduleService.addScheduledRequest(request)
    }

    fun getScheduledPlanWorkoutsRequest(): PlanWorkoutsRequest? {
        return scheduleService.getScheduledRequest(PlanWorkoutsRequest::class.java)
    }

    fun copyWorkouts(request: CopyWorkoutsRequest): CopyWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)
        val targetPlanRepository = planRepositoryStrategy.getRepository(request.targetPlatform)

        val allWorkouts = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts.filter { request.types.contains(it.type) }

        val response = CopyWorkoutsResponse(
            filteredWorkouts.size, allWorkouts.size - filteredWorkouts.size, request.startDate, request.endDate
        )
        val plan = targetPlanRepository.createPlan(request.name, request.startDate, request.planType)
        filteredWorkouts.forEach { targetWorkoutRepository.saveWorkout(it, plan) }
        return response
    }
}
