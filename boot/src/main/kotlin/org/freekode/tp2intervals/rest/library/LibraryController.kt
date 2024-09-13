package org.freekode.tp2intervals.rest.library

import org.freekode.tp2intervals.app.plan.CopyLibraryRequest
import org.freekode.tp2intervals.app.plan.CopyPlanResponse
import org.freekode.tp2intervals.app.plan.LibraryService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LibraryController(
    private val libraryService: LibraryService
) {

    @GetMapping("/api/library-container")
    fun getLibraryContainers(@RequestParam platform: Platform): List<LibraryContainer> {
        return libraryService.findByPlatform(platform)
    }

    @PostMapping("/api/library-container/copy")
    fun copyLibraryContainer(@RequestBody request: CopyLibraryRequest): CopyPlanResponse {
        return libraryService.copyLibrary(request)
    }
}
