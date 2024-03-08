package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.library

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClientConfig
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.CreateTPWorkoutDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPNoteResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.library.TPWorkoutLibraryDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "TrainingPeaksWorkoutLibraryApiClient",
    url = "\${training-peaks.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class]
)
interface TrainingPeaksWorkoutLibraryApiClient {
    @GetMapping("/exerciselibrary/v2/libraries")
    fun getWorkoutLibraries(): List<TPWorkoutLibraryDTO>

    @GetMapping("/exerciselibrary/v2/libraries/{libraryId}/items")
    fun getWorkoutLibraryItems(@PathVariable libraryId: String): List<TPWorkoutLibraryItemDTO>

}
