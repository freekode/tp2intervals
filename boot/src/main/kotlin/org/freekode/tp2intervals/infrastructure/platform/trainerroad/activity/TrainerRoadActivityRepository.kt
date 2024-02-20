package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClient
import org.springframework.stereotype.Repository

@Repository
class TrainerRoadActivityRepository(
    private val trainerRoadApiClient: TrainerRoadApiClient,
    private val trActivityMapper: TRActivityMapper,
) : ActivityRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val memberId = trainerRoadApiClient.getMember().Username
        val activities = trainerRoadApiClient.getActivities(memberId!!, startDate.toString(), endDate.toString())
        return activities
            .map { mapToActivity(it) }
    }

    private fun mapToActivity(it: TrainerRoadActivityDTO): Activity {
        val resource = trainerRoadApiClient.exportFit(it.CompletedRide.WorkoutRecordId.toString())
        return trActivityMapper.mapToActivity(it, resource)
    }

    override fun createActivity(activity: Activity) {
        TODO("Not yet implemented")
    }
}
