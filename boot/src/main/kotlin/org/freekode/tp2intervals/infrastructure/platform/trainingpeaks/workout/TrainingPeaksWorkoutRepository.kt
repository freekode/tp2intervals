package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TPPlanContainerRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.StructureToTPConverter
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["tpWorkoutsCache"])
@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
    private val tpPlanRepository: TPPlanContainerRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val tpWorkoutLibraryRepository: TPWorkoutLibraryRepository,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val objectMapper: ObjectMapper,
) : WorkoutRepository {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun platform() = Platform.TRAINING_PEAKS

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        workouts.forEach { saveWorkoutToCalendar(it) }
    }

    @Cacheable(key = "#libraryContainer.externalData.trainingPeaksId")
    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        return if (libraryContainer.isPlan) {
            getWorkoutsFromTPPlan(libraryContainer)
        } else {
            getWorkoutsFromTPLibrary(libraryContainer)
        }
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = trainingPeaksUserRepository.getUser().userId
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())

        val noteEndDate = getNoteEndDateForFilter(startDate, endDate)
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), noteEndDate.toString())
        val workouts = tpWorkouts.map {
            tpToWorkoutConverter.toWorkout(it)
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
            .find { it.details.externalData == externalData }!!
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
        val createRequest: CreateTPWorkoutDTO
        val structureStr = StructureToTPConverter.toStructureString(objectMapper, workout)
        val athleteId = trainingPeaksUserRepository.getUser().userId
        createRequest = CreateTPWorkoutDTO.planWorkout(
            athleteId, workout, structureStr
        )
        trainingPeaksApiClient.createAndPlanWorkout(athleteId, createRequest)
    }

    private fun getWorkoutsFromTPPlan(libraryContainer: LibraryContainer): List<Workout> {
        val planId = libraryContainer.externalData.trainingPeaksId!!
        val planApplyDate = getPlanApplyDate()
        val response = tpPlanRepository.applyPlan(planId, planApplyDate)

        try {
            val planEndDate = response.endDate.toLocalDate()

            val workoutDateShiftDays = Date.daysDiff(libraryContainer.startDate, planApplyDate)
            val workouts = getWorkoutsFromCalendar(planApplyDate, planEndDate).map {
                it.withDate(
                    it.date!!.minusDays(workoutDateShiftDays.toLong())
                )
            }
            val tpPlan = tpPlanRepository.getPlan(planId)
            assert(tpPlan.workoutCount == workouts.size)
            return workouts
        } catch (e: Exception) {
            throw e
        } finally {
            tpPlanRepository.removeAppliedPlan(response.appliedPlanId)
        }
    }

    private fun getWorkoutsFromTPLibrary(library: LibraryContainer): List<Workout> {
        return tpWorkoutLibraryRepository.getLibraryWorkouts(library.externalData.trainingPeaksId!!)
    }

    private fun getPlanApplyDate(): LocalDate = LocalDate.now()
        .plusDays(trainingPeaksConfigurationRepository.getConfiguration().planDaysShift)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private fun getNoteEndDateForFilter(startDate: LocalDate, endDate: LocalDate): LocalDate =
        if (startDate == endDate) endDate.plusDays(1) else endDate

}
