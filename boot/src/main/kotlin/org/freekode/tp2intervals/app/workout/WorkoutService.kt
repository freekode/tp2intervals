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
    private val trainerRoadCalendarLock = Any()

    fun validateCalendarRequest(request: CopyFromCalendarToCalendarRequest) {
        if (request.refreshSynced) {
            throw PlatformException(request.targetPlatform, "The old refresh option is no longer supported; select the new removal option explicitly")
        }
        if ((request.removeStaleTrainerRoad || request.trainerRoadSyncMode != null) &&
            (request.sourcePlatform != Platform.TRAINER_ROAD || request.targetPlatform != Platform.INTERVALS)) {
            throw PlatformException(request.targetPlatform, "TrainerRoad reconciliation is supported only from TrainerRoad to Intervals.icu")
        }
        if (request.trainerRoadSyncMode != null && request.removeStaleTrainerRoad) {
            throw PlatformException(request.targetPlatform, "Choose either the new sync mode or the legacy removal option")
        }
        if (request.trainerRoadSyncMode == TrainerRoadSyncMode.MIRROR && !request.allowStaleRemoval) {
            throw PlatformException(request.targetPlatform, "Mirror mode requires permission to remove stale TrainerRoad workouts")
        }
        if (request.startDate > request.endDate) {
            throw PlatformException(request.sourcePlatform, "Start date must not be after end date")
        }
    }

    fun copyWorkoutsC2C(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        return if (request.sourcePlatform == Platform.TRAINER_ROAD && request.targetPlatform == Platform.INTERVALS) {
            synchronized(trainerRoadCalendarLock) { executeCalendarCopy(request) }
        } else {
            executeCalendarCopy(request)
        }
    }

    private fun executeCalendarCopy(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to calendar: $request")
        validateCalendarRequest(request)
        if (request.trainerRoadSyncMode != null) {
            val plan = prepareTrainerRoadSync(request)
            if (request.expectedActions != null && request.expectedActions != plan.actions()) {
                throw PlatformException(request.targetPlatform, "The sync plan changed after preview; preview it again before syncing")
            }
            val target = workoutRepositoryMap[request.targetPlatform]!!
            target.saveTrainerRoadWorkoutsToCalendar(plan.toCopy)
            plan.toUpdate.forEach { (source, existing) -> target.updateSyncedWorkout(source, existing) }
            plan.toRemove.forEach { target.deleteSyncedWorkout(it) }
            return plan.response(request)
        }
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

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
                    targets.size == 1 && targetWorkoutRepository.isTrackedTrainerRoadWorkout(targets.single()) -> targets
                    else -> {
                        skippedCount += targets.size
                        emptyList()
                    }
                }
            }
        } else if (request.skipSynced) {
            val plannedWorkouts = if (request.sourcePlatform == Platform.TRAINER_ROAD && request.targetPlatform == Platform.INTERVALS) {
                targetWorkoutRepository.getWorkoutsForReconciliation(request.startDate, request.endDate)
            } else {
                targetWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
            }
            val notSyncedWorkouts = filteredWorkoutsToSync.filter { source ->
                plannedWorkouts.none { target -> target.isSameForSync(source) }
            }
            alreadySyncedCount = filteredWorkoutsToSync.size - notSyncedWorkouts.size
            filteredWorkoutsToSync = notSyncedWorkouts
        }

        if (request.sourcePlatform == Platform.TRAINER_ROAD && request.targetPlatform == Platform.INTERVALS) {
            targetWorkoutRepository.saveTrainerRoadWorkoutsToCalendar(filteredWorkoutsToSync)
        } else {
            targetWorkoutRepository.saveWorkoutsToCalendar(filteredWorkoutsToSync)
        }
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

    fun previewTrainerRoadSync(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        validateCalendarRequest(request)
        if (request.trainerRoadSyncMode == null) {
            throw PlatformException(request.targetPlatform, "A TrainerRoad sync mode is required for preview")
        }
        return prepareTrainerRoadSync(request).response(request)
    }

    private data class TrainerRoadSyncPlan(
        val toCopy: List<Workout>,
        val toUpdate: List<Pair<Workout, Workout>>,
        val toRemove: List<Workout>,
        val alreadySynced: Int,
        val filteredOut: Int,
        val skipped: Int,
    ) {
        fun actions(): List<SyncPlanAction> {
            fun action(kind: SyncAction, workout: Workout, eventId: String?) = SyncPlanAction(
                kind, requireNotNull(workout.date), workout.details.name,
                requireNotNull(workout.details.externalData.trainerRoadId), eventId,
            )
            return (toCopy.map { action(SyncAction.ADD, it, null) } +
                toUpdate.map { (source, target) -> action(SyncAction.UPDATE, source, target.details.externalData.intervalsId) } +
                toRemove.map { action(SyncAction.REMOVE, it, it.details.externalData.intervalsId) })
                .sortedWith(compareBy({ it.date }, { it.action }, { it.trainerRoadId }, { it.intervalsId }))
        }

        fun response(request: CopyFromCalendarToCalendarRequest) = CopyWorkoutsResponse(
            toCopy.size, filteredOut, request.startDate, request.endDate, ExternalData.empty(),
            alreadySynced, toRemove.size, skipped, toUpdate.size, actions(),
        )
    }

    private fun prepareTrainerRoadSync(request: CopyFromCalendarToCalendarRequest): TrainerRoadSyncPlan {
        val mode = requireNotNull(request.trainerRoadSyncMode)
        val source = workoutRepositoryMap[request.sourcePlatform]!!
            .getWorkoutsForReconciliation(request.startDate, request.endDate)
        if (source.any {
                it.date == null || it.date !in request.startDate..request.endDate ||
                    it.details.externalData.trainerRoadId.isNullOrBlank()
            }) {
            throw PlatformException(request.sourcePlatform, "TrainerRoad returned a workout without a valid ID and date; no workouts were changed")
        }
        val startTime = request.defaultStartTime?.takeIf { it.isNotBlank() }?.let {
            try {
                LocalTime.parse(it)
            } catch (e: java.time.format.DateTimeParseException) {
                throw PlatformException(request.targetPlatform, "Invalid default start time: $it")
            }
        }
        val targetRepository = workoutRepositoryMap[request.targetPlatform]!!
        val targets = targetRepository.getWorkoutsForReconciliation(request.startDate, request.endDate)
        val sourceGroups = source.groupBy { it.date to it.details.externalData.trainerRoadId }
        val targetGroups = targets
            .filter { it.date != null && it.date in request.startDate..request.endDate && !it.details.externalData.trainerRoadId.isNullOrBlank() }
            .groupBy { it.date to it.details.externalData.trainerRoadId }
        val toCopy = mutableListOf<Workout>()
        val toUpdate = mutableListOf<Pair<Workout, Workout>>()
        var alreadySynced = 0
        var skipped = 0
        val selected = source.filter { it.details.type in request.types }
        for (workout in selected) {
            val key = workout.date to workout.details.externalData.trainerRoadId
            val matches = targetGroups[key].orEmpty()
            when {
                sourceGroups.getValue(key).size != 1 || matches.size > 1 -> skipped++
                matches.isEmpty() -> toCopy.add(workout.withStartTime(startTime))
                mode == TrainerRoadSyncMode.ADD_MISSING -> alreadySynced++
                !targetRepository.isTrackedTrainerRoadWorkout(matches.single()) -> skipped++
                else -> toUpdate.add(workout.withStartTime(startTime) to matches.single())
            }
        }
        val toRemove = if (mode == TrainerRoadSyncMode.MIRROR) {
            targetGroups.flatMap { (key, matches) ->
                when {
                    key in sourceGroups -> emptyList()
                    matches.size == 1 && targetRepository.isTrackedTrainerRoadWorkout(matches.single()) -> matches
                    else -> {
                        skipped += matches.size
                        emptyList()
                    }
                }
            }
        } else emptyList()
        if ((toUpdate.map { it.second } + toRemove).any { it.details.externalData.intervalsId.isNullOrBlank() }) {
            throw PlatformException(request.targetPlatform, "An Intervals.icu event is missing its ID; no workouts were changed")
        }
        return TrainerRoadSyncPlan(toCopy, toUpdate, toRemove, alreadySynced, source.size - selected.size, skipped)
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
