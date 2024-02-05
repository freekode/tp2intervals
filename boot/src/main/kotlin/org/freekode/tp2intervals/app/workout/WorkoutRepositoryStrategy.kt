package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.springframework.stereotype.Service

@Service
class WorkoutRepositoryStrategy(
    repositories: List<WorkoutRepository>
) {
    private val repositoryMap = repositories.associateBy { it.platform() }

    fun getRepository(platform: Platform): WorkoutRepository = repositoryMap[platform]!!
}