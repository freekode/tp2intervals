package org.freekode.tp2intervals.infrastructure.platform.trainerroad.member

import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.springframework.stereotype.Repository

@Repository
class TRUsernameRepository(
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient,
) {
    fun getMember(): TrainerRoadMemberDTO {
        return trainerRoadMemberApiClient.getMember()
    }

    fun getUsername(): String = getMember().Username!!

    fun getMemberId(): Long = getMember().MemberId
}
