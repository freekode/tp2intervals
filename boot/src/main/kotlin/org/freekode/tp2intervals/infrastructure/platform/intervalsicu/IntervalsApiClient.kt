package org.freekode.tp2intervals.infrastructure.platform.intervalsicu

import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.activity.CreateActivityResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateWorkoutRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
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
    url = "\${app.intervals.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [IntervalsApiClientConfig::class]
)
interface IntervalsApiClient {

    @PostMapping("/api/v1/athlete/{athleteId}/workouts/bulk")
    fun createWorkouts(
        @PathVariable athleteId: String,
        @RequestBody requests: List<CreateWorkoutRequestDTO>
    )

    @PostMapping("/api/v1/athlete/{athleteId}/events")
    fun createEvent(
        @PathVariable athleteId: String,
        @RequestBody createEventRequestDTO: CreateEventRequestDTO
    )

    @GetMapping(
        "/api/v1/athlete/{athleteId}/events?" +
                "oldest={startDate}&" +
                "newest={endDate}&" +
                "resolve=true&" +
                "powerRange={powerRange}&" +
                "hrRange={hrRange}&" +
                "paceRange={paceRange}"
    )
    fun getEvents(
        @PathVariable athleteId: String,
        @PathVariable startDate: String,
        @PathVariable endDate: String,
        @PathVariable powerRange: Float,
        @PathVariable hrRange: Float,
        @PathVariable paceRange: Float,
    ): List<IntervalsEventDTO>

    @GetMapping("/api/v1/athlete/{athleteId}/activities?oldest={startDate}&newest={endDate}")
    fun getActivities(
        @PathVariable("athleteId") athleteId: String,
        @PathVariable("startDate") startDate: String,
        @PathVariable("endDate") endDate: String,
    ): List<IntervalsActivityDTO>

    @PostMapping("/api/v1/athlete/{athleteId}/activities?name={name}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createActivity(
        @PathVariable athleteId: String,
        @PathVariable name: String,
        @RequestPart("file") file: MultipartFile
    ): CreateActivityResponseDTO
}
