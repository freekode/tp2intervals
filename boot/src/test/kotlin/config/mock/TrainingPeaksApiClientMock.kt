package config.mock

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.CreateTPWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPNoteResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutDetailsResponseDTO
import org.springframework.core.io.Resource
import java.io.InputStream

class TrainingPeaksApiClientMock(
    objectMapper: ObjectMapper,
    workoutsResponse: InputStream,
) : TrainingPeaksApiClient {
    private val workouts: List<TPWorkoutCalendarResponseDTO> = objectMapper.readValue(
        workoutsResponse,
        object : TypeReference<List<TPWorkoutCalendarResponseDTO>>() {}) as List<TPWorkoutCalendarResponseDTO>;

    override fun getWorkouts(userId: String, startDate: String, endDate: String) = workouts

    override fun getNotes(userId: String, startDate: String, endDate: String) =
        listOf<TPNoteResponseDTO>()


    override fun downloadWorkoutFit(userId: String, workoutId: String): Resource {
        TODO("Not yet implemented")
    }

    override fun getWorkoutDetails(userId: String, workoutId: String): TPWorkoutDetailsResponseDTO {
        TODO("Not yet implemented")
    }

    override fun downloadWorkoutAttachment(userId: String, workoutId: String, attachmentId: String): Resource {
        TODO("Not yet implemented")
    }

    override fun createAndPlanWorkout(userId: String, requestDTO: CreateTPWorkoutRequestDTO) {
        TODO("Not yet implemented")
    }

    override fun deleteWorkout(userId: String, workoutId: String): Boolean {
        TODO("Not yet implemented")
    }
}
