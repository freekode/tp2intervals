package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.WorkoutStepToTPStructureConverter
import org.springframework.stereotype.Repository


@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient,
    private val tpWorkoutMapper: TPWorkoutMapper,
    private val workoutStepToTPStructureConverter: WorkoutStepToTPStructureConverter
) : WorkoutRepository, ActivityRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun planWorkout(workout: Workout) {
        val structureStr = workoutStepToTPStructureConverter.toWorkoutStructureStr(workout)

        val createRequest = CreateTPWorkoutDTO.planWorkout(
            getUserId(),
            workout.date,
            TPWorkoutTypeMapper.getByType(workout.type),
            workout.title,
            workout.duration?.toMinutes()?.toDouble()?.div(60),
            workout.load,
            structureStr
        )
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    override fun copyWorkout(workout: Workout, plan: Plan) {
        throw PlatformException("TP doesn't support workout copying")
    }

    override fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = getUserId()
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())

        val noteEndDate = getNoteEndDate(startDate, endDate)
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), noteEndDate.toString())
        val workouts = tpWorkouts.map { tpWorkoutMapper.mapToWorkout(it) }
        val notes = tpNotes.map { tpWorkoutMapper.mapToWorkout(it) }
        return workouts + notes
    }

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        TODO("Not yet implemented")
    }

    override fun createActivity(activity: Activity) {
        val createRequest = CreateTPWorkoutDTO.createWorkout(
            getUserId(),
            activity.startedAt,
            TPWorkoutTypeMapper.getByType(activity.type),
            activity.title,
            activity.duration.toMinutes().toDouble().div(60),
            activity.load?.toDouble(),
        )
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun getNoteEndDate(startDate: LocalDate, endDate: LocalDate): LocalDate? =
        if (startDate == endDate) {
            endDate.plusDays(1)
        } else {
            endDate
        }

    private fun getUserId(): String = trainingPeaksApiClient.getUser().userId!!
}
