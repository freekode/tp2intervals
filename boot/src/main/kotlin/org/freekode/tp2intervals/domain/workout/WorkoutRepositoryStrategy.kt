package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.Platform
import org.springframework.stereotype.Service

@Service
class WorkoutRepositoryStrategy(
    repositories: List<WorkoutRepository>
) {
    private val repositoryMap = repositories.associateBy { it.platform() }

    fun getRepository(platform: Platform): WorkoutRepository = repositoryMap[platform]!!
}
