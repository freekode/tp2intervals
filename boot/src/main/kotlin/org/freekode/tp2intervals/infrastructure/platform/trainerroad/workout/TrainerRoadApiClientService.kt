package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TRFindWorkoutsRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.time.LocalDate

@CacheConfig(cacheNames = ["trWorkoutCache"])
@Repository
class TrainerRoadApiClientService(
    private val trainerRoadApiClient: TrainerRoadApiClient,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
) {
    @Cacheable
    fun getWorkout(trWorkoutId: String): Workout {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        val trainerRoadWorkoutConverter = TrainerRoadWorkoutConverter()
        return trainerRoadApiClient.getWorkout(trWorkoutId)
            .let { trainerRoadWorkoutConverter.toWorkout(it, removeHtmlTags) }
    }

    fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.findWorkouts(TRFindWorkoutsRequestDTO(name, 0, 500)).workouts
            .map { TrainerRoadWorkoutConverter().toWorkoutDetails(it, removeHtmlTags) }
    }

    fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate, username: String): List<Workout> {
        return trainerRoadApiClient.getActivities(username, startDate.toString(), endDate.toString())
            .filter { it.activity != null }
            .map { activity ->
                getWorkout(activity.activity!!.id)
                    .withDate(activity.date.toLocalDate())
            }
    }
}
