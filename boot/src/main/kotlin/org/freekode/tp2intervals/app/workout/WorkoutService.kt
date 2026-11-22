package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime

@Service
class WorkoutService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun copyWorkoutsC2C(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to calendar: $request")
        if (request.refreshSynced) {
            throw PlatformException(request.targetPlatform, "The old refresh option is no longer supported; select the new removal option explicitly")
        }
        if (request.removeStaleTrainerRoad && (request.sourcePlatform != Platform.TRAINER_ROAD || request.targetPlatform != Platform.INTERVALS)) {
            throw PlatformException(request.targetPlatform, "Removing stale TrainerRoad workouts is supported only from TrainerRoad to Intervals.icu")
        }
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        if (request.startDate > request.endDate) {
            throw PlatformException(request.sourcePlatform, "Start date must not be after end date")
        }
        val allWorkoutsToSync = if (request.removeStaleTrainerRoad) {
            sourceWorkoutRepository.getWorkoutsForReconciliation(request.startDate, request.endDate)
        } else {
            sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
        }
        val workoutsMatchingTypes = allWorkoutsToSync.filter { request.types.contains(it.details.type) }
        val filteredOutCount = allWorkoutsToSync.size - workoutsMatchingTypes.size
        var filteredWorkoutsToSync = workoutsMatchingTypes
        val defaultStartTime = request.defaultStartTime?.takeIf { it.isNotBlank() }?.let {
            try {
                LocalTime.parse(it)
            } catch (e: Exception) {
                throw PlatformException(request.targetPlatform, "Invalid default start time: $it")
            }
        }
        filteredWorkoutsToSync = filteredWorkoutsToSync.map { it.withStartTime(defaultStartTime) }
        var alreadySyncedCount = 0
        var removedCount = 0
        var skippedCount = 0
        var workoutsToRemove = emptyList<Workout>()
        if (request.removeStaleTrainerRoad) {
            if (allWorkoutsToSync.any {
                    it.date == null || it.date !in request.startDate..request.endDate ||
                        it.details.externalData.trainerRoadId.isNullOrBlank()
                }) {
                throw PlatformException(request.sourcePlatform, "TrainerRoad returned a workout without a valid ID and date; no workouts were deleted")
            }
            val plannedWorkouts = targetWorkoutRepository.getWorkoutsForReconciliation(request.startDate, request.endDate)
            val sourceGroups = allWorkoutsToSync.groupBy { it.date to it.details.externalData.trainerRoadId }
            val targetGroups = plannedWorkouts
                .filter { it.date != null && it.date in request.startDate..request.endDate && !it.details.externalData.trainerRoadId.isNullOrBlank() }
                .groupBy { it.date to it.details.externalData.trainerRoadId }
            filteredWorkoutsToSync = filteredWorkoutsToSync.filter { source ->
                val key = source.date to source.details.externalData.trainerRoadId
                val matches = targetGroups[key].orEmpty()
                when {
                    sourceGroups.getValue(key).size != 1 || matches.size > 1 -> {
                        skippedCount++
                        false
                    }
                    matches.isNotEmpty() -> {
                        alreadySyncedCount++
                        false
                    }
                    else -> true
                }
            }
            workoutsToRemove = targetGroups.flatMap { (key, targets) ->
                when {
                    key in sourceGroups -> emptyList()
                    targets.size == 1 -> targets
                    else -> {
                        skippedCount += targets.size
                        emptyList()
                    }
                }
            }
        } else if (request.skipSynced) {
            val plannedWorkouts = targetWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
            val notSyncedWorkouts = filteredWorkoutsToSync.filter { source ->
                plannedWorkouts.none { target -> target.isSameForSync(source) }
            }
            alreadySyncedCount = filteredWorkoutsToSync.size - notSyncedWorkouts.size
            filteredWorkoutsToSync = notSyncedWorkouts
        }

        targetWorkoutRepository.saveWorkoutsToCalendar(filteredWorkoutsToSync)
        workoutsToRemove.forEach {
            targetWorkoutRepository.deleteSyncedWorkout(it)
            removedCount++
        }
        val response = CopyWorkoutsResponse(
            filteredWorkoutsToSync.size,
            filteredOutCount,
            request.startDate,
            request.endDate,
            ExternalData.empty(), // TODO figure smth better
            alreadySyncedCount,
            removed = removedCount,
            skipped = skippedCount,
        )
        log.info("Saved workouts to calendar successfully: $response")
        return response
    }

    fun copyWorkoutsC2L(request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to library: $request")
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts.filter { request.types.contains(it.details.type) }

        val newPlan = targetPlanRepository.createLibraryContainer(request.name, request.isPlan, request.startDate)
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, filteredWorkouts)
        return CopyWorkoutsResponse(
            filteredWorkouts.size,
            allWorkouts.size - filteredWorkouts.size,
            request.startDate,
            request.endDate,
            newPlan.externalData
        )
    }

    fun copyWorkoutL2L(request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy library to library: $request")
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workout = sourceWorkoutRepository.getWorkoutFromLibrary(request.workoutExternalData)
        targetWorkoutRepository.saveWorkoutsToLibrary(request.targetLibraryContainer, listOf(workout))
        return CopyWorkoutsResponse(1, 0, LocalDate.now(), LocalDate.now(), request.targetLibraryContainer.externalData)
    }

    fun findWorkoutsByName(platform: Platform, name: String): List<WorkoutDetails> {
        log.info("Received request for find workouts by name, platform: $platform, name: $name")
        return workoutRepositoryMap[platform]!!.findWorkoutsFromLibraryByName(name)
    }

    fun deleteWorkoutsFromCalendar(request: DeleteWorkoutRequestDTO) {
        log.info("Received request to delete workouts from calendar: $request")
        val workoutRepository = workoutRepositoryMap[request.platform]!!
        workoutRepository.deleteWorkoutsFromCalendar(request.startDate, request.endDate)
    }
}
