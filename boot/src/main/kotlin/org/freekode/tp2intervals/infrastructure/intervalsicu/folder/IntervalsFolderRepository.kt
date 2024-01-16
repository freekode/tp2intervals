package org.freekode.tp2intervals.infrastructure.intervalsicu.folder

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.config.AppConfigRepository
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanId
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class IntervalsFolderRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val appConfigRepository: AppConfigRepository,
) : PlanRepository {

    override fun createPlan(name: String, startDate: LocalDate): Plan {
        val newFolder = createFolder(name, startDate, FolderDTO.FolderType.PLAN)
        return Plan(PlanId(newFolder.id), newFolder.startDateLocal!!)
    }

    override fun platform() = Platform.INTERVALS

    private fun createFolder(name: String, startDate: LocalDate?, type: FolderDTO.FolderType): FolderDTO {
        val createRequest = CreateFolderRequestDTO(
            0, name, 0, startDate?.toString(), -1, -1, type
        )
        return intervalsApiClient.createFolder(
            appConfigRepository.getConfig().intervalsConfig.athleteId,
            createRequest
        )
    }
}
