package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.springframework.stereotype.Repository


@Repository
class TPWorkoutLibraryRepository(
    private val trainingPeaksWorkoutLibraryApiClient: TrainingPeaksWorkoutLibraryApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
) {

    fun getLibraries(): List<Plan> {
        return trainingPeaksWorkoutLibraryApiClient.getWorkoutLibraries()
            .map { toPlan(it) }
    }

    fun getLibraryItems(libraryId: String): List<Workout> {
        val items = trainingPeaksWorkoutLibraryApiClient.getWorkoutLibraryItems(libraryId)
        return items.map { tpToWorkoutConverter.toWorkout(it) }
    }

    private fun toPlan(libraryDTO: TPWorkoutLibraryDTO): Plan {
        return Plan(
            libraryDTO.libraryName,
            Date.thisMonday(),
            false,
            ExternalData.empty().withTrainingPeaks(libraryDTO.exerciseLibraryId)
        )
    }

}
