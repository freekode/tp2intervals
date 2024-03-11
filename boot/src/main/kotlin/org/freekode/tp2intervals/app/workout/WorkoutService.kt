package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.springframework.stereotype.Service

@Service
class WorkoutService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<PlanRepository>,
) {
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun copyPlannedWorkouts(request: CopyPlannedWorkoutsRequest): CopyPlannedWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val allWorkoutsToPlan = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        var filteredWorkoutsToPlan = allWorkoutsToPlan
            .filter { request.types.contains(it.type) }
        if (request.skipSynced) {
            val plannedWorkouts = targetWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
                .filter { request.types.contains(it.type) }

            filteredWorkoutsToPlan = filteredWorkoutsToPlan
                .filter { !plannedWorkouts.contains(it) }
        }

        val response = CopyPlannedWorkoutsResponse(
            filteredWorkoutsToPlan.size,
            allWorkoutsToPlan.size - filteredWorkoutsToPlan.size,
            request.startDate,
            request.endDate
        )
        filteredWorkoutsToPlan.forEach { targetWorkoutRepository.planWorkout(it) }
        return response
    }

    fun copyPlannedWorkoutsToLibrary(request: CopyPlannedToLibraryWorkoutsRequest): CopyPlannedToLibraryResponse {
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts.filter { request.types.contains(it.type) }

        val plan = targetPlanRepository.createPlan(request.name, request.startDate, request.isPlan)
        filteredWorkouts.forEach { targetWorkoutRepository.saveWorkoutToLibrary(it, plan) }
        return CopyPlannedToLibraryResponse(
            filteredWorkouts.size, allWorkouts.size - filteredWorkouts.size, request.startDate, request.endDate
        )
    }

    fun findWorkoutsByName(platform: Platform, name: String): List<Workout> {
        TODO("not implemented")
    }
}
