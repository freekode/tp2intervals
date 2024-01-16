package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure.WorkoutStepToTPStructureMapper
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val thirdPartyWorkoutMapper: TPWorkoutMapper,
    private val workoutStepToTPStructureMapper: WorkoutStepToTPStructureMapper
) : WorkoutRepository, ActivityRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun planWorkout(workout: Workout, plan: Plan) {
        val structureStr = workoutStepToTPStructureMapper.mapToWorkoutStructureStr(workout)

        val createRequest = CreateTPWorkoutDTO.planWorkout(
            getUserId(),
            workout.date,
            TPWorkoutType.findByType(workout.type).value,
            workout.title,
            workout.duration?.toMinutes()?.toDouble()?.div(60),
            workout.load,
            structureStr
        )
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    override fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = getUserId()
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), endDate.toString())
        val workouts = tpWorkouts.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        val notes = tpNotes.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        return workouts + notes
    }

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        TODO("Not yet implemented")
    }

    override fun createActivity(activity: Activity) {
        val createRequest = CreateTPWorkoutDTO.createWorkout(
            getUserId(),
            activity.startedAt,
            TPWorkoutType.findByType(activity.type).value,
            activity.title,
            activity.duration?.toMinutes()?.toDouble()?.div(60),
            activity.load?.toDouble(),
        )
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun getUserId(): String = trainingPeaksApiClient.getUser().userId!!
}
