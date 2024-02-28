package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanRepositoryStrategy(
    repositories: List<PlanRepository>
) {
    private val repositoryMap = repositories.associateBy { it.platform() }

    fun getRepository(platform: Platform): PlanRepository = repositoryMap[platform]!!
}
