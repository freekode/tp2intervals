package org.freekode.tp2intervals.infrastructure.platform.trainerroad

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity.TrainerRoadActivityMapper
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.findWorkouts(TRFindWorkoutsRequestDTO(name, 0, 500)).workouts
            .map { TrainerRoadWorkoutMapper().toWorkoutDetails(it, removeHtmlTags) }
    }

    fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate, memberId: Long): List<Workout> {
        return trainerRoadApiClient.getTimeline(memberId, startDate.toString(), endDate.toString())
            .plannedActivities
            .filter { it.date.toLocalDate() in startDate..endDate }
            .filter { it.workoutId != null }
            .map { plannedActivity ->
                getWorkout(plannedActivity.workoutId!!.toString())
                    .withDate(plannedActivity.date.toLocalDate())
            }
    }

    @Cacheable
    fun getWorkout(trWorkoutId: String): Workout {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        val trainerRoadWorkoutMapper = TrainerRoadWorkoutMapper()
        return trainerRoadApiClient.getWorkout(trWorkoutId)
            .let { trainerRoadWorkoutMapper.toWorkout(it, removeHtmlTags) }
            .also { workout ->
                log.debug(
                    "Mapped TrainerRoad workout {}, target preview: {}",
                    trWorkoutId,
                    targetPreview(workout),
                )
            }
    }

    fun getActivities(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val activityIds = trainerRoadApiClient.getTimeline(memberId, startDate.toString(), endDate.toString())
            .activities
            .filter {
                val startedDate = it.started?.toLocalDate()
                startedDate != null && startedDate in startDate..endDate
            }
            .map { it.id }

        if (activityIds.isEmpty()) {
            return emptyList()
        }

        val activities = trainerRoadApiClient.getActivities(memberId, activityIds.joinToString(","))
            .filter { it.date.toLocalDate() in startDate..endDate }
        val activityMapper = TrainerRoadActivityMapper()
        return activities.map { mapToActivity(activityMapper, it) }
    }

    private fun mapToActivity(activityMapper: TrainerRoadActivityMapper, it: TrainerRoadActivityDTO): Activity {
        val activityId = it.completedRide?.WorkoutRecordId ?: it.activityId
        val resource = trainerRoadApiClient.exportFit(activityId.toString())
        return activityMapper.mapToActivity(it, resource)
    }

    private fun targetPreview(workout: Workout): String {
        return workout.structure?.steps
            ?.filterIsInstance<SingleStep>()
            ?.take(8)
            ?.joinToString { "${it.name}:${it.target.start}-${it.target.end}" }
            ?: "no structure"
    }
}
