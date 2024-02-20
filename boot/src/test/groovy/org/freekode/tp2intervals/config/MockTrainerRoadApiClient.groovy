package org.freekode.tp2intervals.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.jetbrains.annotations.NotNull
import org.springframework.core.io.Resource

class MockTrainerRoadApiClient implements TrainerRoadApiClient {
    private final List<TrainerRoadActivityDTO> activities
    private final Resource file
    private final TRWorkoutResponseDTO trWorkoutResponseDTO

    MockTrainerRoadApiClient(
            ObjectMapper objectMapper,
            String eventsResponse,
            Resource file,
            String workoutDetailsResponse) {
        this.file = file
        activities = objectMapper.readValue(eventsResponse, new TypeReference<List<TrainerRoadActivityDTO>>() {
        }) as List<TrainerRoadActivityDTO>
        trWorkoutResponseDTO = objectMapper.readValue(workoutDetailsResponse, TRWorkoutResponseDTO.class)
    }

    @Override
    TrainerRoadMemberDTO getMember() {
        return new TrainerRoadMemberDTO(123L, "my-user")
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
    TRWorkoutResponseDTO getWorkoutDetails(String workoutId) {
        return trWorkoutResponseDTO
    }
}
