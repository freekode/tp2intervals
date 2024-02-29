package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TPPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.StructureToTPConverter
import org.springframework.stereotype.Repository


@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
    private val tpPlanRepository: TPPlanRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val objectMapper: ObjectMapper,
) : WorkoutRepository, ActivityRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun planWorkout(workout: Workout) {
        val structureStr = toStructureString(workout)
        val createRequest = CreateTPWorkoutDTO.planWorkout(getUserId(), workout, structureStr)
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    override fun getWorkout(id: String): Workout {
        throw PlatformException(Platform.TRAINING_PEAKS, "TP doesn't support not planned workout view")
    }

    override fun getWorkouts(plan: Plan): List<Workout> {
        val planId = plan.externalData.trainingPeaksId!!
        val tpPlan = tpPlanRepository.getPlan(planId)
        val daysShift = trainingPeaksConfigurationRepository.getConfiguration().planDaysShift
        val response = tpPlanRepository.applyPlan(planId, LocalDate.now().plusDays(daysShift))

        try {
            val planStartDate = LocalDateTime.parse(response.startDate).toLocalDate()
            val planEndDate = LocalDateTime.parse(response.endDate).toLocalDate()
            val workouts = getPlannedWorkouts(planStartDate, planEndDate)
            assert(tpPlan.workoutCount == workouts.size)
            return workouts
        } catch (e: Exception) {
            throw e
        } finally {
            tpPlanRepository.removeAppliedPlan(response.appliedPlanId)
        }
    }

    override fun saveWorkout(workout: Workout, plan: Plan) {
        throw PlatformException(Platform.TRAINING_PEAKS, "TP doesn't support workout copying")
    }

    override fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = getUserId()
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())

        val noteEndDate = getNoteEndDateForFilter(startDate, endDate)
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), noteEndDate.toString())
        val workouts = tpWorkouts.map { tpToWorkoutConverter.toWorkout(it) }
        val notes = tpNotes.map { tpToWorkoutConverter.toWorkout(it) }
        return workouts + notes
    }

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        TODO("Not yet implemented")
    }

    override fun createActivity(activity: Activity) {
        val createRequest = CreateTPWorkoutDTO.createActivity(getUserId(), activity)
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun toStructureString(workout: Workout) =
        if (workout.structure != null) {
            StructureToTPConverter(objectMapper, workout.structure).toTPStructureStr()
        } else {
            null
        }

    private fun getNoteEndDateForFilter(startDate: LocalDate, endDate: LocalDate): LocalDate? =
        if (startDate == endDate) {
            endDate.plusDays(1)
        } else {
            endDate
        }

    private fun getUserId() = trainingPeaksUserRepository.getUserId()
}
