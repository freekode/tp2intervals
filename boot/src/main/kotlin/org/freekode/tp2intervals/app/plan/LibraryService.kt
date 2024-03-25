package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.springframework.stereotype.Service

@Service
class LibraryService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun getLibraryContainers(platform: Platform): List<LibraryContainer> {
        val repository = planRepositoryMap[platform]!!
        return repository.getLibraryContainers()
    }

    fun copyLibrary(request: CopyLibraryRequest): CopyPlanResponse {
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workouts = sourceWorkoutRepository.getWorkoutsFromLibrary(request.libraryContainer)
        val newPlan = targetPlanRepository.createLibraryContainer(request.newName, Date.thisMonday(), true)
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, workouts)
        return CopyPlanResponse(newPlan.name, workouts.size)
    }
}
