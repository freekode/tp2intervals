package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.freekode.tp2intervals.infrastructure.PlatformException
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

    fun getWorkoutsFromCalendar(
        startDate: LocalDate,
        endDate: LocalDate,
        memberId: Long,
        requireCompleteTimeline: Boolean = false,
    ): List<Workout> {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        val timeline = trainerRoadApiClient.getCalendarTimeline(memberId)
        if (requireCompleteTimeline &&
            (timeline.start == null || timeline.end == null ||
                timeline.start > startDate.atStartOfDay() || timeline.end < endDate.plusDays(1).atStartOfDay())) {
            throw PlatformException(Platform.TRAINER_ROAD, "TrainerRoad calendar does not cover the requested date range; no workouts were deleted")
        }
        val plannedActivities = timeline.plannedActivities.filter { it.date.toLocalDate() in startDate..endDate }
        if (requireCompleteTimeline && plannedActivities.any { it.workoutId == null }) {
            throw PlatformException(Platform.TRAINER_ROAD, "A planned TrainerRoad activity has no workout ID; cannot safely reconcile this date range")
        }
        return plannedActivities
            .filter { it.workoutId != null }
            .map { plannedActivity ->
                val id = plannedActivity.workoutId!!.toString()
                val workout = getWorkout(id, removeHtmlTags)
                if (requireCompleteTimeline && workout.details.externalData.trainerRoadId != id) {
                    throw PlatformException(Platform.TRAINER_ROAD, "TrainerRoad returned a workout with an unexpected ID; no workouts were deleted")
                }
                workout.withDate(plannedActivity.date.toLocalDate())
            }
    }

    @Cacheable(key = "#trWorkoutId + ':' + #removeHtmlTags")
    fun getWorkout(trWorkoutId: String, removeHtmlTags: Boolean): Workout {
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
