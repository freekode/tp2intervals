package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
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
    fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.findWorkouts(TRFindWorkoutsRequestDTO(name, 0, 500)).workouts
            .map { TrainerRoadWorkoutMapper().toWorkoutDetails(it, removeHtmlTags) }
    }

    fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate, username: String): List<Workout> {
        return trainerRoadApiClient.getActivities(username, startDate.toString(), endDate.toString())
            .filter { it.activity != null }
            .map { activity ->
                getWorkout(activity.activity!!.id)
                    .withDate(activity.date.toLocalDate())
            }
    }

    @Cacheable
    fun getWorkout(trWorkoutId: String): Workout {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        val trainerRoadWorkoutMapper = TrainerRoadWorkoutMapper()
        return trainerRoadApiClient.getWorkout(trWorkoutId)
            .let { trainerRoadWorkoutMapper.toWorkout(it, removeHtmlTags) }
    }

    fun getActivities(username: String, startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val activities = trainerRoadApiClient.getActivities(username, startDate.toString(), endDate.toString())
        val activityMapper = TrainerRoadActivityMapper()
        return activities.map { mapToActivity(activityMapper, it) }
    }

    private fun mapToActivity(activityMapper: TrainerRoadActivityMapper, it: TrainerRoadActivityDTO): Activity {
        val resource = trainerRoadApiClient.exportFit(it.completedRide!!.WorkoutRecordId.toString())
        return activityMapper.mapToActivity(it, resource)
    }
}
