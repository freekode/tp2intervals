package org.freekode.tp2intervals.config


import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.member.TrainerRoadMemberApiClient

class MockTrainerRoadMemberApiClient implements TrainerRoadMemberApiClient {
    @Override
    TrainerRoadMemberDTO getMember() {
        return new TrainerRoadMemberDTO(123L, "my-user")
    }
}
