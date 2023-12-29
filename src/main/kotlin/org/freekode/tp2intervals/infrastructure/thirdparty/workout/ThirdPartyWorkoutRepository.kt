package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyApiClient
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class ThirdPartyWorkoutRepository(
    private val thirdPartyApiClient: ThirdPartyApiClient,
    private val thirdPartyWorkoutMapper: ThirdPartyWorkoutMapper,
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
        val structureStr = thirdPartyWorkoutMapper.mapToWorkoutStructureStr(workout)

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

    fun createActivity(workout: Workout) {
        val createRequest = CreateThirdPartyWorkoutDTO.createWorkout(
            getUserId(),
            workout.date,
            ThirdPartyWorkoutType.findByType(workout.type).value,
            workout.title,
            workout.duration?.toMinutes()?.toDouble()?.div(60),
            workout.load,
        )
        thirdPartyApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun getUserId(): String = thirdPartyApiClient.getUser().userId!!
}
