package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyApiClient
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure.WorkoutStepToThirdPartyStructureMapper
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class ThirdPartyWorkoutRepository(
    private val thirdPartyApiClient: ThirdPartyApiClient,
    private val thirdPartyWorkoutMapper: ThirdPartyWorkoutMapper,
    private val workoutStepToThirdPartyStructureMapper: WorkoutStepToThirdPartyStructureMapper
) {
    fun getWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = getUserId()
        val tpWorkouts = thirdPartyApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())
        val tpNotes = thirdPartyApiClient.getNotes(userId, startDate.toString(), endDate.toString())
        val workouts = tpWorkouts.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        val notes = tpNotes.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        return workouts + notes
    }

    fun planWorkout(workout: Workout) {
        val structureStr = workoutStepToThirdPartyStructureMapper.mapToWorkoutStructureStr(workout)

        val createRequest = CreateThirdPartyWorkoutDTO.planWorkout(
            getUserId(),
            workout.date,
            ThirdPartyWorkoutType.findByType(workout.type).value,
            workout.title,
            workout.duration?.toMinutes()?.toDouble()?.div(60),
            workout.load,
            structureStr
        )
        thirdPartyApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    fun createActivity(activity: Activity) {
        val createRequest = CreateThirdPartyWorkoutDTO.createWorkout(
            getUserId(),
            activity.startedAt,
            ThirdPartyWorkoutType.findByType(activity.type).value,
            activity.title,
            activity.duration?.toMinutes()?.toDouble()?.div(60),
            activity.load,
        )
        thirdPartyApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun getUserId(): String = thirdPartyApiClient.getUser().userId!!
}
