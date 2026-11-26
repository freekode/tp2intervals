package org.freekode.tp2intervals.infrastructure.platform.trainerroad.member

import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["trUsernameCache"])
@Repository
class TRUsernameRepository(
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient,
) {
    fun getMember(): TrainerRoadMemberDTO =
        trainerRoadMemberApiClient.getMember()

    @Cacheable(cacheNames = ["trMemberIdCache"], key = "'singleton'")
    fun getMemberId(): Long =
        trainerRoadMemberApiClient.getMember().MemberId

    @Cacheable(key = "'singleton'")
    fun getUsername(): String {
        return trainerRoadMemberApiClient.getMember().Username!!
    }
}
