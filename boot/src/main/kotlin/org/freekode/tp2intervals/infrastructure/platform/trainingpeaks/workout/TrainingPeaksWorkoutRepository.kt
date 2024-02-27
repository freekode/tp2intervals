package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.StructureToTPConverter
import org.springframework.stereotype.Repository


@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
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

    private fun getUserId(): String = trainingPeaksApiClient.getUser().userId!!
}
