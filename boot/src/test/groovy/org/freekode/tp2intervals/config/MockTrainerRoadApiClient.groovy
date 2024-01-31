package org.freekode.tp2intervals.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.trainerroad.TrainerRoadMemberDTO
import org.freekode.tp2intervals.infrastructure.trainerroad.activity.TrainerRoadActivityDTO
import org.springframework.core.io.Resource

class MockTrainerRoadApiClient implements TrainerRoadApiClient {
    private final List<TrainerRoadActivityDTO> activities
    private final Resource file

    MockTrainerRoadApiClient(ObjectMapper objectMapper, String eventsResponse, Resource file) {
        this.file = file
        activities = objectMapper.readValue(eventsResponse, new TypeReference<List<TrainerRoadActivityDTO>>() {
        }) as List<TrainerRoadActivityDTO>
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
}
