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
            (request.sourcePlatform != Platform.TRAINER_ROAD ||
                request.targetPlatform !in listOf(Platform.INTERVALS, Platform.TRAINING_PEAKS))) {
            throw PlatformException(request.targetPlatform, "TrainerRoad reconciliation is supported only to Intervals.icu or TrainingPeaks")
        }
        if (request.removeStaleTrainerRoad) {
            throw PlatformException(request.targetPlatform, "The old removal option is no longer supported; select Mirror and allow removal explicitly")
        }
        if (request.trainerRoadSyncMode == TrainerRoadSyncMode.MIRROR && !request.allowStaleRemoval) {
            throw PlatformException(request.targetPlatform, "Mirror mode requires permission to remove stale TrainerRoad workouts")
        }
        if (request.startDate > request.endDate) {
            throw PlatformException(request.sourcePlatform, "Start date must not be after end date")
        }
    }

    fun copyWorkoutsC2C(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        return if (request.sourcePlatform == Platform.TRAINER_ROAD &&
            request.targetPlatform in listOf(Platform.INTERVALS, Platform.TRAINING_PEAKS)) {
            synchronized(trainerRoadCalendarLock) { executeCalendarCopy(request) }
        } else {
            executeCalendarCopy(request)
        }
    }

    private fun executeCalendarCopy(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        log.info("Received calendar sync: source={}, target={}, mode={}",
            request.sourcePlatform, request.targetPlatform, request.trainerRoadSyncMode)
        validateCalendarRequest(request)
        if (request.trainerRoadSyncMode != null) {
            val plan = prepareTrainerRoadSync(request)
            if (request.expectedActions != null && request.expectedActions != plan.actions()) {
                throw PlatformException(request.targetPlatform, "The sync plan changed after preview; preview it again before syncing")
            }
            if (request.trainerRoadSyncMode == TrainerRoadSyncMode.MIRROR && request.expectedActions != null &&
                request.expectedUnidentifiedActivityDates?.distinct()?.sorted() != plan.unidentifiedActivityDates) {
                throw PlatformException(request.targetPlatform, "The unidentified TrainerRoad activities changed after preview; preview again before syncing")
            }
            val target = workoutRepositoryMap[request.targetPlatform]!!
            target.saveTrainerRoadWorkoutsToCalendar(plan.toCopy)
            plan.toUpdate.forEach { (source, existing) -> target.updateSyncedWorkout(source, existing) }
            plan.toRemove.forEach { target.deleteSyncedWorkout(it) }
            return plan.response(request)
        }
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val allWorkoutsToSync = sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
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
        filteredWorkoutsToSync = filteredWorkoutsToSync.map {
            it.withStartTime(if (request.sourcePlatform == Platform.TRAINER_ROAD) it.startTime ?: defaultStartTime else defaultStartTime)
        }
        var alreadySyncedCount = 0
        if (request.skipSynced) {
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
        val response = CopyWorkoutsResponse(
            filteredWorkoutsToSync.size,
            filteredOutCount,
            request.startDate,
            request.endDate,
            ExternalData.empty(), // TODO figure smth better
            alreadySyncedCount,
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
        val matchedWorkouts: List<MatchedSyncWorkout>,
        val filteredOut: Int,
        val skippedWorkouts: List<SkippedSyncWorkout>,
        val unidentifiedActivityDates: List<LocalDate>,
    ) {
        fun actions(): List<SyncPlanAction> {
            fun action(kind: SyncAction, workout: Workout, target: Workout?) = SyncPlanAction(
                kind, requireNotNull(workout.date), workout.details.name,
                requireNotNull(workout.details.externalData.trainerRoadId),
                target?.details?.externalData?.intervalsId,
                target?.details?.externalData?.trainingPeaksId,
            )
            return (toCopy.map { action(SyncAction.ADD, it, null) } +
                toUpdate.map { (source, target) -> action(SyncAction.UPDATE, source, target) } +
                toRemove.map { action(SyncAction.REMOVE, it, it) })
                .sortedWith(compareBy({ it.date }, { it.action }, { it.trainerRoadId }, { it.intervalsId }, { it.trainingPeaksId }))
        }

        fun response(request: CopyFromCalendarToCalendarRequest) = CopyWorkoutsResponse(
            toCopy.size, filteredOut, request.startDate, request.endDate, ExternalData.empty(),
            matchedWorkouts.size, toRemove.size, skippedWorkouts.size, toUpdate.size, actions(), unidentifiedActivityDates, skippedWorkouts,
            matchedWorkouts,
        )
    }

    private fun prepareTrainerRoadSync(request: CopyFromCalendarToCalendarRequest): TrainerRoadSyncPlan {
        val mode = requireNotNull(request.trainerRoadSyncMode)
        val sourceResult = workoutRepositoryMap[request.sourcePlatform]!!
            .getTrainerRoadReconciliationSource(request.startDate, request.endDate)
        val source = sourceResult.workouts
        val unidentifiedDates = sourceResult.unidentifiedActivityDates.filter { it in request.startDate..request.endDate }
        if (unidentifiedDates.isNotEmpty()) {
            log.warn("Skipping {} TrainerRoad activities without workout IDs", unidentifiedDates.size)
        }
        if (source.any {
                it.date == null || it.date !in request.startDate..request.endDate ||
                    (it.details.externalData.trainerRoadId?.toLongOrNull() ?: 0) <= 0
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
        fun eligible(workout: Workout) = targetRepository.hasExplicitTrainerRoadId(workout)
        val targets = targetRepository.getWorkoutsForReconciliation(request.startDate, request.endDate)
        val sourceGroups = source.groupBy { it.date to it.details.externalData.trainerRoadId }
        val targetGroups = targets
            .filter { it.date != null && it.date in request.startDate..request.endDate && !it.details.externalData.trainerRoadId.isNullOrBlank() }
            .groupBy { it.date to it.details.externalData.trainerRoadId }
        val ambiguousTargetGroups = targets
            .filter { it.date != null && it.date in request.startDate..request.endDate &&
                !it.details.externalData.hasClearTrainerRoadId(it.details.description) }
            .flatMap { target ->
                ExternalData.trainerRoadIdsInDescription(target.details.description)
                    .map { id -> (target.date to id) to target }
            }
            .groupBy({ it.first }, { it.second })
        val toCopy = mutableListOf<Workout>()
        val toUpdate = mutableListOf<Pair<Workout, Workout>>()
        val matched = mutableListOf<MatchedSyncWorkout>()
        val skipped = mutableListOf<SkippedSyncWorkout>()
        fun skip(workout: Workout, reason: SkippedSyncReason, matchingTargets: List<Workout>) {
            skipped.add(SkippedSyncWorkout(
                requireNotNull(workout.date), workout.details.name,
                requireNotNull(workout.details.externalData.trainerRoadId),
                matchingTargets.mapNotNull { it.details.externalData.intervalsId },
                reason,
                matchingTargets.mapNotNull { it.details.externalData.trainingPeaksId },
            ))
        }
        fun recordMatch(workout: Workout) {
            matched.add(MatchedSyncWorkout(
                requireNotNull(workout.date), workout.details.name,
                requireNotNull(workout.details.externalData.trainerRoadId),
            ))
        }
        val selected = source.filter { it.details.type in request.types }
        for (sourceWorkout in selected) {
            val workout = sourceWorkout.withStartTime(sourceWorkout.startTime ?: startTime)
            val key = workout.date to workout.details.externalData.trainerRoadId
            val matches = targetGroups[key].orEmpty()
            val ambiguousMatches = ambiguousTargetGroups[key].orEmpty()
            when {
                sourceGroups.getValue(key).size != 1 -> skip(workout, SkippedSyncReason.DUPLICATE_TRAINERROAD_ID, matches)
                ambiguousMatches.isNotEmpty() -> skip(workout, SkippedSyncReason.PROTECTED_EVENT,
                    (matches + ambiguousMatches).distinctBy { it.details.externalData })
                matches.size > 1 -> skip(workout, SkippedSyncReason.DUPLICATE_INTERVALS_ID, matches)
                matches.isEmpty() -> toCopy.add(workout)
                mode == TrainerRoadSyncMode.ADD_MISSING -> recordMatch(workout)
                !eligible(matches.single()) -> skip(workout, SkippedSyncReason.PROTECTED_EVENT, matches)
                targetRepository.isTrainerRoadWorkoutUpToDate(workout, matches.single()) -> recordMatch(workout)
                else -> toUpdate.add(workout to matches.single())
            }
        }
        val toRemove = if (mode == TrainerRoadSyncMode.MIRROR) {
            targetGroups.flatMap { (key, matches) ->
                when {
                    key in sourceGroups -> emptyList()
                    ambiguousTargetGroups[key].orEmpty().isNotEmpty() -> {
                        matches.forEach { skip(it, SkippedSyncReason.PROTECTED_EVENT, listOf(it)) }
                        emptyList()
                    }
                    matches.size == 1 && eligible(matches.single()) -> matches
                    else -> {
                        val reason = if (matches.size > 1) SkippedSyncReason.DUPLICATE_INTERVALS_ID else SkippedSyncReason.PROTECTED_EVENT
                        matches.forEach { skip(it, reason, listOf(it)) }
                        emptyList()
                    }
                }
            }
        } else emptyList()
        if ((toUpdate.map { it.second } + toRemove).any {
                when (request.targetPlatform) {
                    Platform.INTERVALS -> it.details.externalData.intervalsId.isNullOrBlank()
                    Platform.TRAINING_PEAKS -> it.details.externalData.trainingPeaksId.isNullOrBlank()
                    else -> true
                }
            }) {
            throw PlatformException(request.targetPlatform, "A destination workout is missing its ID; no workouts were changed")
        }
        return TrainerRoadSyncPlan(toCopy, toUpdate, toRemove, matched, source.size - selected.size, skipped, unidentifiedDates.distinct().sorted())
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
