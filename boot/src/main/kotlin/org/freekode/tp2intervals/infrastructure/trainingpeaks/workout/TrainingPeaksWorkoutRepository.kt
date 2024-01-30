package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure.WorkoutStepToTPStructureMapper
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val thirdPartyWorkoutMapper: TPWorkoutMapper,
    private val workoutStepToTPStructureMapper: WorkoutStepToTPStructureMapper
) {
    fun getWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = getUserId()
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), endDate.toString())
        val workouts = tpWorkouts.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        val notes = tpNotes.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        return workouts + notes
    }

    fun planWorkout(workout: Workout) {
        val structureStr = workoutStepToTPStructureMapper.mapToWorkoutStructureStr(workout)

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

    fun createActivity(activity: Activity) {
        val createRequest = CreateTPWorkoutDTO.createWorkout(
            getUserId(),
            activity.startedAt,
            TPWorkoutTypeMapper.getByType(activity.type),
            activity.title,
            activity.duration?.toMinutes()?.toDouble()?.div(60),
            activity.load,
        )
        trainingPeaksApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun getUserId(): String = trainingPeaksApiClient.getUser().userId!!
}
