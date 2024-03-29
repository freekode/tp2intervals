package org.freekode.tp2intervals.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.activity.CreateActivityResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder.CreateFolderRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder.FolderDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.jetbrains.annotations.NotNull
import org.springframework.web.multipart.MultipartFile

class MockIntervalsApiClient implements IntervalsApiClient {
    List<IntervalsEventDTO> events

    MockIntervalsApiClient(ObjectMapper objectMapper, String eventsResponse) {
        events = objectMapper.readValue(eventsResponse, new TypeReference<List<IntervalsEventDTO>>() {
        }) as List<IntervalsEventDTO>
    }

    @Override
    void createWorkouts(String athleteId, List<CreateWorkoutRequestDTO> requests) {
    }

    @Override
    List<IntervalsEventDTO> getEvents(String athleteId, String startDate, String endDate, float powerRange, float hrRange, float paceRange) {
        return events
    }

    @Override
    List<IntervalsActivityDTO> getActivities(String athleteId, String startDate, String endDate) {
        return null
    }

    @Override
    CreateActivityResponseDTO createActivity(String athleteId, MultipartFile file) {
        return null
    }

    @Override
    void createEvent(String athleteId, CreateEventRequestDTO createEventRequestDTO) {

    }
}
