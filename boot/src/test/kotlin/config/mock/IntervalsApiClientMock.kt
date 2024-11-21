package config.mock

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.activity.CreateActivityResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.springframework.web.multipart.MultipartFile

class IntervalsApiClientMock(
    objectMapper: ObjectMapper,
    eventsResponse: InputStream
) : IntervalsApiClient {
    private val events: List<IntervalsEventDTO> = objectMapper.readValue(
        eventsResponse,
        object : TypeReference<List<IntervalsEventDTO>>() {}) as List<IntervalsEventDTO>;

    override fun createWorkouts(athleteId: String, requests: List<CreateWorkoutRequestDTO>) {
        TODO("Not yet implemented")
    }

    override fun createEvent(athleteId: String, createEventRequestDTO: CreateEventRequestDTO) {
        TODO("Not yet implemented")
    }

    override fun getEvents(
        athleteId: String,
        startDate: String,
        endDate: String,
        powerRange: Float,
        hrRange: Float,
        paceRange: Float
    ): List<IntervalsEventDTO> = events


    override fun getActivities(athleteId: String, startDate: String, endDate: String): List<IntervalsActivityDTO> {
        TODO("Not yet implemented")
    }

    override fun createActivity(athleteId: String, name: String, file: MultipartFile): CreateActivityResponseDTO {
        TODO("Not yet implemented")
    }
}
