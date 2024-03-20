package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.infrastructure.Signature
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["libraryItemsCache"])
@Repository
class IntervalsFolderContainerRepository(
    private val intervalsFolderApiClient: IntervalsFolderApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository
) : LibraryContainerRepository {

    override fun platform() = Platform.INTERVALS

    override fun createLibraryContainer(name: String, startDate: LocalDate, isPlan: Boolean): LibraryContainer {
        val folderType = if (isPlan) "PLAN" else "FOLDER"
        val newFolder = createFolder(name, startDate, folderType)
        return toPlan(newFolder)
    }

    @Cacheable(key = "'INTERVALS'")
    override fun getLibraryContainer(): List<LibraryContainer> {
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

    private fun toPlan(folderDTO: FolderDTO): LibraryContainer {
        return if (folderDTO.type == "PLAN") {
            LibraryContainer(folderDTO.name, folderDTO.startDateLocal!!, true, ExternalData.empty().withIntervals(folderDTO.id))
        } else {
            LibraryContainer.planFromMonday(folderDTO.name, ExternalData.empty().withIntervals(folderDTO.id))
        }
    }
}
