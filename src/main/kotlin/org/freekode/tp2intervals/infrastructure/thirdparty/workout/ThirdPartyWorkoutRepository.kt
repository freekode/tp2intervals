package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyApiClient
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class ThirdPartyWorkoutRepository(
    private val thirdPartyApiClient: ThirdPartyApiClient,
    private val thirdPartyWorkoutMapper: ThirdPartyWorkoutMapper,
    private val objectMapper: ObjectMapper
) {
    fun getWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = getUserId()
        val tpWorkouts = thirdPartyApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())
        val tpNotes = thirdPartyApiClient.getNotes(userId, startDate.toString(), endDate.toString())
        val workouts = tpWorkouts.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        val notes = tpNotes.map { thirdPartyWorkoutMapper.mapToWorkout(it) }
        return workouts + notes
    }

    fun createAndPlanWorkout(workout: Workout) {
        val structure = thirdPartyWorkoutMapper.mapToWorkoutStructure(workout)
        val structureStr = objectMapper.copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(structure)

        val createRequest = CreateThirdPartyWorkoutDTO(
            getUserId(),
            workout.scheduledDate,
            ThirdPartyWorkoutType.findByType(workout.type).value,
            workout.title,
            workout.duration?.toMinutes()?.toDouble()?.div(60),
            workout.load,
            structureStr
        )
        thirdPartyApiClient.createAndPlanWorkout(getUserId(), createRequest)
    }

    private fun getUserId(): String = thirdPartyApiClient.getUser().userId!!
}
