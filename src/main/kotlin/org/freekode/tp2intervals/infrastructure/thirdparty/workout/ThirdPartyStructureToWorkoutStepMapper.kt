package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.Workout
import org.springframework.stereotype.Repository

@Repository
class ThirdPartyStructureToWorkoutStepMapper(
    private val objectMapper: ObjectMapper,
) {

    fun mapToWorkoutStep(workout: Workout): String? {
        throw RuntimeException("i cant yet")
    }
}
