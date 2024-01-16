package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.springframework.stereotype.Service

@Service
class PlanRepositoryStrategy(
    repositories: List<PlanRepository>
) {
    private val repositoryMap = repositories.associateBy { it.platform() }

    fun getRepository(platform: Platform): PlanRepository = repositoryMap[platform]!!
}
