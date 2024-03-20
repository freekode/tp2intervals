package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TRFindWorkoutsRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.springframework.stereotype.Repository


@Repository
class TrainerRoadWorkoutRepository(
    private val trainerRoadApiClient: TrainerRoadApiClient,
) : WorkoutRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun getWorkoutFromLibrary(workoutDetails: WorkoutDetails): Workout {
        val trWorkout = trainerRoadApiClient.getWorkoutDetails(workoutDetails.externalData.trainerRoadId!!)
        return TRWorkoutConverter().toWorkout(trWorkout)
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        return trainerRoadApiClient.findWorkouts(TRFindWorkoutsRequestDTO(name, 0, 500)).workouts
            .map { TRWorkoutConverter().toWorkoutDetails(it) }
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        TODO("Not yet implemented")
    }

    override fun saveWorkoutToCalendar(workout: Workout) {
        throw PlatformException(Platform.TRAINER_ROAD, "TR doesn't support workout planning")
    }

    override fun saveWorkoutToLibrary(libraryContainer: LibraryContainer, workout: Workout) {
        throw PlatformException(Platform.TRAINER_ROAD, "TR doesn't support workout creation")
    }

    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        throw PlatformException(Platform.TRAINER_ROAD, "TR has only one library, search by name")
    }

}
