package org.freekode.tp2intervals.infrastructure.intervalsicu.folder

import org.freekode.tp2intervals.domain.plan.Folder
import org.freekode.tp2intervals.domain.plan.FolderId
import org.freekode.tp2intervals.infrastructure.config.ConfigurationRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsApiClient
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class IntervalsFolderRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val configurationRepository: ConfigurationRepository,
) {

    fun createPlan(name: String, startDate: LocalDate): Folder {
        val newFolder = createFolder(name, startDate, FolderDTO.FolderType.PLAN)
        return Folder(FolderId(newFolder.id), newFolder.startDateLocal!!)
    }

    private fun createFolder(name: String, startDate: LocalDate?, type: FolderDTO.FolderType): FolderDTO {
        val createRequest = CreateFolderRequestDTO(
            0, name, 0, startDate?.toString(), -1, -1, type
        )
        return intervalsApiClient.createFolder(
            configurationRepository.getConfiguration().intervalsAthleteId,
            createRequest
        )
    }
}
