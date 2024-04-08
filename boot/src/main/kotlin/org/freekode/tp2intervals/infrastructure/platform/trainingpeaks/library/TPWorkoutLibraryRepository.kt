package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["tpWorkoutsCache"])
@Repository
class TPWorkoutLibraryRepository(
    private val trainingPeaksWorkoutLibraryApiClient: TrainingPeaksWorkoutLibraryApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
) {

    @Cacheable(key = "'singleton'")
    fun getAllWorkouts(): List<Workout> {
        return getLibraries()
            .flatMap { getLibraryWorkouts(it.externalData.trainingPeaksId!!) }
    }

    fun getLibraries(): List<LibraryContainer> {
        return trainingPeaksWorkoutLibraryApiClient.getWorkoutLibraries()
            .map { toPlan(it) }
    }

    @Cacheable
    fun getLibraryWorkouts(libraryId: String): List<Workout> {
        val items = trainingPeaksWorkoutLibraryApiClient.getWorkoutLibraryItems(libraryId)
        return items.map { tpToWorkoutConverter.toWorkout(it) }
    }

    private fun toPlan(libraryDTO: TPWorkoutLibraryDTO): LibraryContainer {
        return LibraryContainer(
            "${libraryDTO.libraryName} (${libraryDTO.ownerName})",
            Date.thisMonday(),
            false,
            0,
            ExternalData.empty().withTrainingPeaks(libraryDTO.exerciseLibraryId)
        )
    }

}
