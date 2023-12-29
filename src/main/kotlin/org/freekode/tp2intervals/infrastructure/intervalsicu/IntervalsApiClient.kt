package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "IntervalsApiClient",
    url = "https://intervals.icu",
    dismiss404 = true,
    configuration = [IntervalsApiClientConfig::class]
)
interface IntervalsApiClient {

    @PostMapping("/api/v1/athlete/{athleteId}/folders")
    fun createFolder(
        @PathVariable("athleteId") athleteId: String,
        @RequestBody createFolderRequestDTO: CreateFolderRequestDTO
    ): FolderDTO

    @PostMapping("/api/v1/athlete/{athleteId}/workouts")
    fun createWorkout(
        @PathVariable("athleteId") athleteId: String,
        @RequestBody createWorkoutRequestDTO: CreateWorkoutRequestDTO
    )

    @GetMapping("/api/v1/athlete/{athleteId}/events?oldest={startDate}&newest={endDate}")
    fun getEvents(
        @PathVariable("athleteId") athleteId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String,
    ): List<IntervalsEventDTO>

    @GetMapping("/api/v1/athlete/{athleteId}/activities?oldest={startDate}&newest={endDate}")
    fun getActivities(
        @PathVariable("athleteId") athleteId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String,
    ): List<IntervalsActivityDTO>
}
