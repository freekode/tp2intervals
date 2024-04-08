package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library

import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    value = "TrainingPeaksWorkoutLibraryApiClient",
    url = "\${app.training-peaks.api-url}",
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
