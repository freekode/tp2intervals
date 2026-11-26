package org.freekode.tp2intervals.domain.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer

data class TrainerRoadReconciliationSource(
    val workouts: List<Workout>,
    val unidentifiedActivityDates: List<LocalDate>,
)

interface WorkoutRepository {
    fun platform(): Platform

    fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout>

    fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout>

    fun getWorkoutFromLibrary(externalData: ExternalData): Workout

    fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails>

    fun saveWorkoutsToCalendar(workouts: List<Workout>)

    fun saveTrainerRoadWorkoutsToCalendar(workouts: List<Workout>) {
        throw UnsupportedOperationException("Saving TrainerRoad workouts is not supported for ${platform()}")
    }

    fun hasExplicitTrainerRoadId(workout: Workout): Boolean = false

    fun isTrainerRoadWorkoutUpToDate(source: Workout, target: Workout): Boolean {
        throw UnsupportedOperationException("Comparing TrainerRoad workouts is not supported for ${platform()}")
    }

    fun getWorkoutsForReconciliation(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        throw UnsupportedOperationException("Calendar reconciliation is not supported for ${platform()}")
    }

    fun getTrainerRoadReconciliationSource(startDate: LocalDate, endDate: LocalDate): TrainerRoadReconciliationSource {
        throw UnsupportedOperationException("TrainerRoad reconciliation is not supported for ${platform()}")
    }

    fun deleteSyncedWorkout(workout: Workout) {
        throw UnsupportedOperationException("Deleting synced workouts is not supported for ${platform()}")
    }

    fun updateSyncedWorkout(source: Workout, target: Workout) {
        throw UnsupportedOperationException("Updating synced workouts is not supported for ${platform()}")
    }

    fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>)

    fun deleteWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate)
}
