package org.freekode.tp2intervals.rest.library

import org.freekode.tp2intervals.app.plan.CopyLibraryRequest
import org.freekode.tp2intervals.app.plan.CopyPlanResponse
import org.freekode.tp2intervals.app.plan.LibraryService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LibraryController(
    private val libraryService: LibraryService
) {

    @GetMapping("/api/library")
    fun getLibraries(@RequestParam platform: Platform): List<Plan> {
        return libraryService.getLibraries(platform)
    }

    @PostMapping("/api/library/copy")
    fun copyLibrary(@RequestBody request: CopyLibraryRequest): CopyPlanResponse {
        return libraryService.copyLibrary(request)
    }
}
