package config.mock

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TRFindWorkoutsRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRFindWorkoutsResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.springframework.core.io.Resource

class TrainerRoadApiClientMock(
    objectMapper: ObjectMapper,
    simpleWorkoutResponse: InputStream,
    complexWorkoutResponse: InputStream,
    anotherWorkoutResponse: InputStream,
) : TrainerRoadApiClient {
    private val simpleWorkout: TRWorkoutResponseDTO = objectMapper.readValue(
        simpleWorkoutResponse, TRWorkoutResponseDTO::class.java
    )
    private val complexWorkout: TRWorkoutResponseDTO = objectMapper.readValue(
        complexWorkoutResponse, TRWorkoutResponseDTO::class.java
    )
    private val anotherWorkout: TRWorkoutResponseDTO = objectMapper.readValue(
        anotherWorkoutResponse, TRWorkoutResponseDTO::class.java
    )

    override fun getActivities(username: String, startDate: String, endDate: String): List<TrainerRoadActivityDTO> {
        TODO("Not yet implemented")
    }

    override fun findWorkouts(requestDTO: TRFindWorkoutsRequestDTO): TRFindWorkoutsResponseDTO {
        TODO("Not yet implemented")
    }

    override fun getWorkout(workoutId: String): TRWorkoutResponseDTO {
        return when (workoutId) {
            "simple" -> simpleWorkout
            "complex" -> complexWorkout
            "another" -> anotherWorkout
            else -> throw IllegalStateException("unknown workout id: $workoutId")
        }
    }

    override fun exportFit(activityId: String): Resource {
        TODO("Not yet implemented")
    }
}
