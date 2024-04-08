package org.freekode.tp2intervals.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryItemDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TrainingPeaksWorkoutLibraryApiClient

class MockTrainingPeaksWorkoutLibraryApiClient implements TrainingPeaksWorkoutLibraryApiClient {

    private final List<TPWorkoutLibraryItemDTO> workouts

    MockTrainingPeaksWorkoutLibraryApiClient(
            ObjectMapper objectMapper,
            String eventsResponse) {
        this.workouts = objectMapper.readValue(eventsResponse, new TypeReference<List<TPWorkoutLibraryItemDTO>>() {
        }) as List<TPWorkoutLibraryItemDTO>
    }

    @Override
    List<TPWorkoutLibraryDTO> getWorkoutLibraries() {
        return null
    }

    @Override
    List<TPWorkoutLibraryItemDTO> getWorkoutLibraryItems(String libraryId) {
        return workouts
    }
}
