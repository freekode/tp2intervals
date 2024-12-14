package config.mock

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.CreateTPWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPNoteResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.springframework.core.io.Resource
import java.io.InputStream

class TrainingPeaksApiClientMock(
    objectMapper: ObjectMapper,
    swimWorkoutResponse: InputStream,
) : TrainingPeaksApiClient {
    private val swimWorkout: TPWorkoutCalendarResponseDTO = objectMapper.readValue(
        swimWorkoutResponse, TPWorkoutCalendarResponseDTO::class.java
    )

    override fun getWorkouts(userId: String, startDate: String, endDate: String) =
        listOf(swimWorkout)

    override fun getNotes(userId: String, startDate: String, endDate: String) =
        listOf<TPNoteResponseDTO>()


    override fun downloadWorkoutFit(userId: String, workoutId: String): Resource {
        TODO("Not yet implemented")
    }

    override fun createAndPlanWorkout(userId: String, requestDTO: CreateTPWorkoutRequestDTO) {
        TODO("Not yet implemented")
    }

    override fun deleteWorkout(userId: String, workoutId: String): Boolean {
        TODO("Not yet implemented")
    }
}
