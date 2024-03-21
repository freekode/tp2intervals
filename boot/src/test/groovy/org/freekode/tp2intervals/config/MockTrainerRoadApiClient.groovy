package org.freekode.tp2intervals.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TRFindWorkoutsRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRFindWorkoutsResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.jetbrains.annotations.NotNull
import org.springframework.core.io.Resource

class MockTrainerRoadApiClient implements TrainerRoadApiClient {
    private final Resource file
    private final List<TrainerRoadActivityDTO> activities
    private final TRWorkoutResponseDTO trWorkoutResponseDTOAbney
    private final TRWorkoutResponseDTO trWorkoutResponseDTOObelisk

    MockTrainerRoadApiClient(
            ObjectMapper objectMapper,
            Resource file,
            String eventsResponse,
            String workoutDetailsResponseAbney,
            String workoutDetailsResponseObelisk) {
        this.file = file
        this.activities = objectMapper.readValue(eventsResponse, new TypeReference<List<TrainerRoadActivityDTO>>() {
        }) as List<TrainerRoadActivityDTO>
        this.trWorkoutResponseDTOAbney = objectMapper.readValue(workoutDetailsResponseAbney, TRWorkoutResponseDTO.class)
        this.trWorkoutResponseDTOObelisk = objectMapper.readValue(workoutDetailsResponseObelisk, TRWorkoutResponseDTO.class)
    }

    @Override
    List<TrainerRoadActivityDTO> getActivities(String memberId, String startDate, String endDate) {
        return activities
    }

    @Override
    Resource exportFit(String activityId) {
        return file
    }

    @Override
    TRWorkoutResponseDTO getWorkout(String workoutId) {
        switch (workoutId) {
            case "abney": return trWorkoutResponseDTOAbney
            case "obelisk": return trWorkoutResponseDTOObelisk
        }
        return null
    }

    @Override
    TRFindWorkoutsResponseDTO findWorkouts(TRFindWorkoutsRequestDTO requestDTO) {
        return null
    }
}
