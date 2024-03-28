package org.freekode.tp2intervals.infrastructure.platform.wahoosystm

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.springframework.stereotype.Repository


@Repository
class WahooSystmWorkoutRepository(
) : WorkoutRepository {
    override fun platform() = Platform.WAHOO_SYSTM

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        TODO("Not yet implemented")
    }

    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        TODO("Not yet implemented")
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        TODO("Not yet implemented")
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        TODO("Not yet implemented")
    }

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        TODO("Not yet implemented")
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        TODO("Not yet implemented")
    }
}
