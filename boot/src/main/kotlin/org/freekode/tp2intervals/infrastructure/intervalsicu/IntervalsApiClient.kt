package org.freekode.tp2intervals.infrastructure.intervalsicu

import org.freekode.tp2intervals.infrastructure.intervalsicu.activity.CreateActivityResponseDTO
import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.CreateFolderRequestDTO
import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.FolderDTO
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.CreateWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsEventDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@FeignClient(
    value = "IntervalsApiClient",
    url = "\${intervals.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [IntervalsApiClientConfig::class]
)
interface IntervalsApiClient {

    @GetMapping("/api/v1/athlete/{athleteId}")
    fun getAthlete(@PathVariable("athleteId") athleteId: String): Map<String, Any>

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

    @PostMapping("/api/v1/athlete/{athleteId}/events")
    fun createEvent(
        @PathVariable athleteId: String,
        @RequestBody createEventRequestDTO: CreateEventRequestDTO
    )

    @GetMapping("/api/v1/athlete/{athleteId}/events?oldest={startDate}&newest={endDate}&resolve=true")
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

    @PostMapping("/api/v1/athlete/{athleteId}/activities", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createActivity(
        @PathVariable athleteId: String,
        @RequestPart("file") file: MultipartFile
    ): CreateActivityResponseDTO
}
