package org.freekode.tp2intervals.app.activity

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.springframework.stereotype.Service

@Service
class ActivityRepositoryStrategy(
    repositories: List<ActivityRepository>
) {
    private val repositoryMap = repositories.associateBy { it.platform() }

    fun getRepository(platform: Platform): ActivityRepository = repositoryMap[platform]!!
}
