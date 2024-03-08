package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.library

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["tpWorkoutLibraryItemsCache"])
@Repository
class TPWorkoutLibraryRepository(
    private val trainingPeaksWorkoutLibraryApiClient: TrainingPeaksWorkoutLibraryApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
) {

    @Cacheable(key = "'singleton'")
    fun getAllWorkoutsFromLibraries(): List<Workout> {
        return getLibraries()
            .map { getLibraryItems(it.exerciseLibraryId) }
            .flatten()
    }

    private fun getLibraries(): List<TPWorkoutLibraryDTO> {
        return trainingPeaksWorkoutLibraryApiClient.getWorkoutLibraries();
    }

    private fun getLibraryItems(libraryId: String): List<Workout> {
        val items = trainingPeaksWorkoutLibraryApiClient.getWorkoutLibraryItems(libraryId)
        return items.map { tpToWorkoutConverter.toWorkout(it) }
    }
}
