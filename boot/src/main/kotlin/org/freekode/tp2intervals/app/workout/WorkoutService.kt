package org.freekode.tp2intervals.app.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO
import org.springframework.stereotype.Service

@Service
class WorkoutService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun copyWorkoutsFromCalendarToCalendar(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val allWorkoutsToSync = sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
        var filteredWorkoutsToSync = allWorkoutsToSync.filter { request.types.contains(it.details.type) }
        if (request.skipSynced) {
            val plannedWorkouts = targetWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
                .filter { request.types.contains(it.details.type) }

            filteredWorkoutsToSync = filteredWorkoutsToSync.filter { !plannedWorkouts.contains(it) }
        }

        val response = CopyWorkoutsResponse(
            filteredWorkoutsToSync.size,
            allWorkoutsToSync.size - filteredWorkoutsToSync.size,
            request.startDate,
            request.endDate,
            ExternalData.empty() // TODO figure smth better
        )
        targetWorkoutRepository.saveWorkoutsToCalendar(filteredWorkoutsToSync)
        return response
    }

    fun copyWorkoutsFromCalendarToLibrary(request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts.filter { request.types.contains(it.details.type) }

        val newPlan = targetPlanRepository.createLibraryContainer(request.name, request.startDate, request.isPlan)
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, filteredWorkouts)
        return CopyWorkoutsResponse(
            filteredWorkouts.size,
            allWorkouts.size - filteredWorkouts.size,
            request.startDate,
            request.endDate,
            newPlan.externalData
        )
    }

    fun copyWorkoutFromLibraryToLibrary(request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workout = sourceWorkoutRepository.getWorkoutFromLibrary(request.workoutDetails.externalData)
        targetWorkoutRepository.saveWorkoutsToLibrary(request.targetLibraryContainer, listOf(workout))
        return CopyWorkoutsResponse(1, 0, LocalDate.now(), LocalDate.now(), request.targetLibraryContainer.externalData)
    }

    fun findWorkoutsByName(platform: Platform, name: String): List<WorkoutDetails> {
        return workoutRepositoryMap[platform]!!.findWorkoutsFromLibraryByName(name)
    }

    fun deleteWorkoutsFromCalendar(request: DeleteWorkoutRequestDTO) {
        val workoutRepository = workoutRepositoryMap[request.platform]!!
        workoutRepository.deleteWorkoutsFromCalendar(request.startDate, request.endDate)
    }
}
