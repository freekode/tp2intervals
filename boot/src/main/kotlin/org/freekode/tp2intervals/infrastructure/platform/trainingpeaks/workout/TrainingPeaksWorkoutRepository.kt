package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.CatchFeignException
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksReconciliationApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanCoachApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.ToTPStructureConverter
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


@CacheConfig(cacheNames = ["tpWorkoutsCache"])
@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val trainingPeaksPlanCoachApiClient: TrainingPeaksPlanCoachApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
    private val trainingPeaksPlanRepository: TrainingPeaksPlanRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val tpWorkoutLibraryRepository: TPWorkoutLibraryRepository,
    private val tpAttachmentService: TPAttachmentService,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val objectMapper: ObjectMapper,
    private val reconciliationApiClient: TrainingPeaksReconciliationApiClient,
) : WorkoutRepository {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun platform() = Platform.TRAINING_PEAKS

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        workouts.forEach { saveWorkoutToCalendar(it) }
    }

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun saveTrainerRoadWorkoutsToCalendar(workouts: List<Workout>) {
        if (workouts.any { it.date == null || (it.details.externalData.trainerRoadId?.toLongOrNull() ?: 0) <= 0 }) {
            throw PlatformException(Platform.TRAINER_ROAD, "Cannot save a workout without a valid TrainerRoad ID and date")
        }
        val userId = trainingPeaksUserRepository.getUser().userId
        val requests = workouts.map { createRequest(userId, it) }
        requests.forEach { request ->
            val result = reconciliationApiClient.createWorkout(userId, request)
            if ((result.path("workoutId").asText().toLongOrNull() ?: 0) <= 0) {
                throw PlatformException(Platform.TRAINING_PEAKS, "TrainingPeaks did not confirm the workout ID; check the calendar before retrying")
            }
        }
    }

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun getWorkoutsForReconciliation(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = trainingPeaksUserRepository.getUser().userId
        return reconciliationApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())
            .map { toReconciliationWorkout(it) }
    }

    override fun hasExplicitTrainerRoadId(workout: Workout): Boolean =
        !workout.protectedFromTrainerRoadSync &&
            workout.details.externalData.hasClearTrainerRoadId(workout.details.description)

    override fun isTrainerRoadWorkoutUpToDate(source: Workout, target: Workout): Boolean {
        val expected = createRequest(trainingPeaksUserRepository.getUser().userId, source)
        val targetStructure = target.structure?.let { ToTPStructureConverter.toStructureString(objectMapper, it) }
        return source.date == target.date &&
            expected.title == target.details.name &&
            expected.workoutTypeValueId == TPTrainingTypeMapper.getByType(target.details.type) &&
            expected.description == target.details.description &&
            source.details.duration?.toMinutes() == target.details.duration?.toMinutes() &&
            expected.tssPlanned == target.details.load &&
            (source.startTime == null || source.startTime == target.startTime) &&
            expected.structure == targetStructure
    }

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun updateSyncedWorkout(source: Workout, target: Workout) {
        val userId = trainingPeaksUserRepository.getUser().userId
        val id = target.details.externalData.trainingPeaksId
            ?: throw PlatformException(Platform.TRAINING_PEAKS, "Cannot update a workout without a TrainingPeaks ID")
        val current = reconciliationApiClient.getWorkout(userId, id)
        val fresh = toReconciliationWorkout(current)
        if (!hasExplicitTrainerRoadId(fresh) || fresh.date != source.date ||
            fresh.details.externalData.trainerRoadId != source.details.externalData.trainerRoadId ||
            fresh.details.externalData.trainingPeaksId != id) {
            throw PlatformException(Platform.TRAINING_PEAKS, "The workout changed or was completed; preview again before syncing")
        }
        val fields = objectMapper.valueToTree<ObjectNode>(createRequest(userId, source))
        listOf("title", "workoutTypeValueId", "description", "totalTimePlanned", "tssPlanned", "structure").forEach { field ->
            current.set<com.fasterxml.jackson.databind.JsonNode>(field, fields.get(field) ?: objectMapper.nullNode())
        }
        source.startTime?.let {
            current.put("startTimePlanned", requireNotNull(source.date).atTime(it).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        }
        val result = reconciliationApiClient.updateWorkout(userId, id, current)
        if (result.path("workoutId").asText() != id) {
            throw PlatformException(Platform.TRAINING_PEAKS, "TrainingPeaks did not confirm the workout update; check the calendar before retrying")
        }
    }

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
    override fun deleteSyncedWorkout(workout: Workout) {
        val userId = trainingPeaksUserRepository.getUser().userId
        val id = workout.details.externalData.trainingPeaksId
            ?: throw PlatformException(Platform.TRAINING_PEAKS, "Cannot delete a workout without a TrainingPeaks ID")
        val fresh = toReconciliationWorkout(reconciliationApiClient.getWorkout(userId, id))
        if (!hasExplicitTrainerRoadId(fresh) || fresh.date != workout.date ||
            fresh.details.externalData.trainerRoadId != workout.details.externalData.trainerRoadId ||
            fresh.details.externalData.trainingPeaksId != id) {
            throw PlatformException(Platform.TRAINING_PEAKS, "The workout changed or was completed; preview again before syncing")
        }
        if (reconciliationApiClient.deleteWorkout(userId, id) != true) {
            throw PlatformException(Platform.TRAINING_PEAKS, "TrainingPeaks did not confirm deletion; check the calendar before retrying")
        }
    }

    private fun toReconciliationWorkout(data: ObjectNode): Workout {
        val dto = objectMapper.treeToValue(data, TPWorkoutCalendarResponseDTO::class.java)
        val workout = tpToWorkoutConverter.toWorkout(dto)
        val completed = data.get("completed")
        val actualFields = listOf("totalTime", "startTime", "distance", "tssActual", "calories", "powerAverage")
        val otherActualFields = listOf(
            "heartRateMinimum", "heartRateMaximum", "heartRateAverage", "velocityAverage", "velocityMaximum",
            "normalizedSpeedActual", "normalizedPowerActual", "powerMaximum", "energy", "elevationGain",
            "elevationLoss", "elevationMinimum", "elevationAverage", "elevationMaximum", "torqueAverage",
            "torqueMaximum", "tempMin", "tempAvg", "tempMax", "cadenceAverage", "cadenceMaximum", "rpe", "feeling",
        )
        val plannedTime = data.get("startTimePlanned")
        var plannedTimeKnown = plannedTime != null
        val plannedDateTime = when {
            plannedTime == null || plannedTime.isNull -> null
            !plannedTime.isTextual -> {
                plannedTimeKnown = false
                null
            }
            else -> try {
                LocalDateTime.parse(plannedTime.asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: DateTimeParseException) {
                plannedTimeKnown = false
                null
            }
        }
        if (plannedDateTime != null && plannedDateTime.toLocalDate() != workout.date) plannedTimeKnown = false
        val protected = completed == null || (!completed.isNull && (!completed.isBoolean || completed.asBoolean())) ||
            actualFields.any { !data.has(it) || !data.get(it).isNull } ||
            otherActualFields.any { data.hasNonNull(it) } || !plannedTimeKnown ||
            (dto.structure?.structure?.isNotEmpty() == true && workout.structure == null)
        return workout.copy(
            details = workout.details.copy(description = dto.description),
            startTime = plannedDateTime?.toLocalTime(),
            protectedFromTrainerRoadSync = protected,
        )
    }

    private fun createRequest(userId: String, workout: Workout): CreateTPWorkoutRequestDTO {
        val structure = workout.structure?.let { ToTPStructureConverter.toStructureString(objectMapper, it) }
        return CreateTPWorkoutRequestDTO.planWorkout(userId, workout, structure).apply {
            description = listOfNotNull(
                workout.details.description?.takeIf { it.isNotBlank() },
                workout.details.externalData.toSimpleString(),
            ).joinToString("\n\n")
            if (!workout.details.externalData.hasClearTrainerRoadId(description)) {
                throw PlatformException(Platform.TRAINING_PEAKS, "Cannot sync a workout whose description conflicts with the TrainerRoad ID marker")
            }
            startTimePlanned = workout.startTime?.let {
                requireNotNull(workout.date).atTime(it).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }
    }

    @Cacheable(key = "#libraryContainer.externalData.trainingPeaksId")
    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        val user = trainingPeaksUserRepository.getUser()
        return if (libraryContainer.isPlan) {
            if (user.isAthlete) {
                getWorkoutsFromTPPlan(libraryContainer)
            } else {
                getWorkoutsFromTPCoachPlan(libraryContainer)
            }
        } else {
            getWorkoutsFromTPLibrary(libraryContainer)
        }
    }

    private fun getWorkoutsFromTPCoachPlan(libraryContainer: LibraryContainer): List<Workout> {
        val planWorkouts = trainingPeaksPlanCoachApiClient.getPlanWorkouts(
            libraryContainer.externalData.trainingPeaksId!!,
            libraryContainer.startDate.minusYears(10).toString(),
            libraryContainer.startDate.plusYears(2).toString()
        )
        val planNotes = trainingPeaksPlanCoachApiClient.getPlanNotes(
            libraryContainer.externalData.trainingPeaksId,
            libraryContainer.startDate.minusYears(10).toString(),
            libraryContainer.startDate.plusYears(2).toString()
        )

        val workouts = planWorkouts.map {
            tpToWorkoutConverter.toWorkout(it)
        }

        val notes = planNotes.map { tpToWorkoutConverter.toWorkout(it) }
        return workouts + notes
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = trainingPeaksUserRepository.getUser().userId
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())

        val noteEndDate = getNoteEndDateForFilter(startDate, endDate)
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), noteEndDate.toString())
        val workouts = tpWorkouts.map {
            val attachments = tpAttachmentService.getAttachments(userId, it.id)
            tpToWorkoutConverter.toWorkout(it, attachments)
        }

        val notes = tpNotes.map { tpToWorkoutConverter.toWorkout(it) }
        return workouts + notes
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        return tpWorkoutLibraryRepository.getAllWorkouts()
            .map { it.details }
            .filter { it.name.contains(name) }
    }

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        return tpWorkoutLibraryRepository.getAllWorkouts()
            .find {
                val data = it.details.externalData
                when {
                    externalData.trainingPeaksId != null -> externalData.trainingPeaksId == data.trainingPeaksId
                    externalData.intervalsId != null -> externalData.intervalsId == data.intervalsId
                    externalData.trainerRoadId != null -> externalData.trainerRoadId == data.trainerRoadId
                    else -> false
                }
            }!!
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        throw PlatformException(Platform.TRAINING_PEAKS, "TP doesn't support workout creation")
    }

    override fun deleteWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate) {
        val userId = trainingPeaksUserRepository.getUser().userId
        getWorkoutsFromCalendar(startDate, endDate).forEach {
            trainingPeaksApiClient.deleteWorkout(userId, it.details.externalData.trainingPeaksId!!)
        }
    }

    private fun saveWorkoutToCalendar(workout: Workout) {
        val createRequest: CreateTPWorkoutRequestDTO
        val structureStr = if (workout.structure != null) ToTPStructureConverter.toStructureString(objectMapper, workout.structure) else null
        val athleteId = trainingPeaksUserRepository.getUser().userId
        createRequest = CreateTPWorkoutRequestDTO.planWorkout(
            athleteId, workout, structureStr
        )
        trainingPeaksApiClient.createAndPlanWorkout(athleteId, createRequest)
    }

    private fun getWorkoutsFromTPPlan(libraryContainer: LibraryContainer): List<Workout> {
        val planId = libraryContainer.externalData.trainingPeaksId!!
        val planStartDateShift = trainingPeaksConfigurationRepository.getConfiguration().planDaysShift
        val planStartDate = Date.thisMonday()
        val planApplyDate = planStartDate.plusDays(planStartDateShift)
        val response = trainingPeaksPlanRepository.applyPlan(planId, planApplyDate)

        try {
            val planEndDate = response.endDate.toLocalDate()

            val workouts = getWorkoutsFromCalendar(planApplyDate, planEndDate).map {
                it.withDate(it.date!!.minusDays(planStartDateShift))
            }
            return workouts
        } catch (e: Exception) {
            throw e
        } finally {
            trainingPeaksPlanRepository.removeAppliedPlan(response.appliedPlanId)
        }
    }

    private fun getWorkoutsFromTPLibrary(library: LibraryContainer): List<Workout> {
        return tpWorkoutLibraryRepository.getLibraryWorkouts(library.externalData.trainingPeaksId!!)
    }

    private fun getNoteEndDateForFilter(startDate: LocalDate, endDate: LocalDate): LocalDate =
        if (startDate == endDate) endDate.plusDays(1) else endDate

}
