package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.folder

import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "IntervalsFolderApiClient",
    url = "\${app.intervals.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [IntervalsApiClientConfig::class]
)
interface IntervalsFolderApiClient {

    @PostMapping("/api/v1/athlete/{athleteId}/folders")
    fun createFolder(
        @PathVariable("athleteId") athleteId: String,
        @RequestBody createFolderRequestDTO: CreateFolderRequestDTO
    ): FolderDTO

    @GetMapping("/api/v1/athlete/{athleteId}/folders")
    fun getFolders(
        @PathVariable("athleteId") athleteId: String,
    ): List<FolderDTO>

    @DeleteMapping("/api/v1/athlete/{athleteId}/folders/{folderId}")
    fun deleteFolder(
        @PathVariable("athleteId") athleteId: String,
        @PathVariable("folderId") folderId: String
    )
}
