package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.domain.workout.structure.StepModifier
import org.springframework.stereotype.Service

@Service
class LibraryService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun findByPlatform(platform: Platform): List<LibraryContainer> {
        val repository = planRepositoryMap[platform]!!
        return repository.getLibraryContainers()
    }

    fun copyLibrary(request: CopyLibraryRequest): CopyPlanResponse {
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workouts = sourceWorkoutRepository.getWorkoutsFromLibrary(request.libraryContainer)
            .map { it.addWorkoutStepModifier(request.stepModifier) }
        val newPlan = targetPlanRepository.createLibraryContainer(
            request.newName,
            request.libraryContainer.isPlan,
            workouts.first().date
        )
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, workouts)
        return CopyPlanResponse(newPlan.name, workouts.size, newPlan.externalData)
    }

    fun deleteLibrary(request: DeleteLibraryRequest) {
        val planRepository = planRepositoryMap[request.platform]!!
        planRepository.deleteLibraryContainer(request.externalData)
    }

    fun create(request: CreateLibraryContainerRequest): LibraryContainer {
        val planRepository = planRepositoryMap[request.platform]!!
        return planRepository.createLibraryContainer(request.name, false, null)
    }

    private fun Workout.addWorkoutStepModifier(stepModifier: StepModifier): Workout =
        Workout(details, date, structure?.addModifier(stepModifier))
}
