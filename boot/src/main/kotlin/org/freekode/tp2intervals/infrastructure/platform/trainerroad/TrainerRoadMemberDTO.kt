package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import com.fasterxml.jackson.annotation.JsonAlias

class TrainerRoadMemberDTO(
    @JsonAlias("memberId")
    val MemberId: Long,
    @JsonAlias("username")
    val Username: String?,
)
