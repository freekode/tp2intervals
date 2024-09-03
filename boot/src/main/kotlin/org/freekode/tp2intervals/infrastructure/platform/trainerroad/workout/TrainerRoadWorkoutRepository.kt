package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class TrainerRoadWorkoutRepository(
    private val trUsernameRepository: TRUsernameRepository,
    private val trainerRoadApiClientService: TrainerRoadApiClientService,
) : WorkoutRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        return trainerRoadApiClientService.getWorkout(externalData.trainerRoadId!!)
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        return trainerRoadApiClientService.findWorkoutsFromLibraryByName(name)
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val username = trUsernameRepository.getUsername()
        return trainerRoadApiClientService.getWorkoutsFromCalendar(startDate, endDate, username)
    }

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        throw PlatformException(Platform.TRAINER_ROAD, "TR doesn't support workout planning")
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        throw PlatformException(Platform.TRAINER_ROAD, "TR doesn't support workout creation")
    }

    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        throw PlatformException(Platform.TRAINER_ROAD, "TR has only one library, search by name")
    }

    override fun deleteWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate) {
        TODO("Not implemented")
    }

}
