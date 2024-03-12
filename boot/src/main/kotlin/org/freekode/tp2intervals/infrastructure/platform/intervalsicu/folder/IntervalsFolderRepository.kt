package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.plan.LibraryRepository
import org.freekode.tp2intervals.infrastructure.Signature
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["libraryItemsCache"])
@Repository
class IntervalsFolderRepository(
    private val intervalsFolderApiClient: IntervalsFolderApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository
) : LibraryRepository {

    override fun platform() = Platform.INTERVALS

    override fun createPlan(name: String, startDate: LocalDate, isPlan: Boolean): Plan {
        val folderType = if (isPlan) "PLAN" else "FOLDER"
        val newFolder = createFolder(name, startDate, folderType)
        return toPlan(newFolder)
    }

    @Cacheable(key = "'INTERVALS'")
    override fun getLibraryItems(): List<Plan> {
        return intervalsFolderApiClient.getFolders(intervalsConfigurationRepository.getConfiguration().athleteId)
            .map { toPlan(it) }
    }

    private fun createFolder(name: String, startDate: LocalDate?, type: String): FolderDTO {
        val createRequest = CreateFolderRequestDTO(
            0, name, Signature.description, 0, startDate?.toString(), -1, -1, type
        )
        return intervalsFolderApiClient.createFolder(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            createRequest
        )
    }

    private fun toPlan(folderDTO: FolderDTO): Plan {
        return if (folderDTO.type == "PLAN") {
            Plan(folderDTO.name, folderDTO.startDateLocal!!, true, ExternalData.empty().withIntervals(folderDTO.id))
        } else {
            Plan.planFromMonday(folderDTO.name, ExternalData.empty().withIntervals(folderDTO.id))
        }
    }
}
