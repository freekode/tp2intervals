package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.PlanRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.springframework.stereotype.Repository


@Repository
class IntervalsFolderRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository
) : PlanRepository {
    private val planDescription = """
        Created by tp2intervals (https://github.com/freekode/tp2intervals)
    """.trimIndent()

    override fun platform() = Platform.INTERVALS

    override fun createPlan(name: String, startDate: LocalDate, isPlan: Boolean): Plan {
        val folderType = if (isPlan) "PLAN" else "FOLDER"
        val newFolder = createFolder(name, startDate, folderType)
        return Plan(newFolder.name, newFolder.startDateLocal!!, ExternalData.empty().withIntervals(newFolder.id))
    }

    override fun getPlans(): List<Plan> {
        TODO("Not yet implemented")
    }

    private fun createFolder(name: String, startDate: LocalDate?, type: String): FolderDTO {
        val createRequest = CreateFolderRequestDTO(
            0, name, planDescription, 0, startDate?.toString(), -1, -1, type
        )
        return intervalsApiClient.createFolder(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            createRequest
        )
    }
}
