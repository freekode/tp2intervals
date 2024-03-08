package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.activity

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.CreateTPWorkoutDTO
import org.springframework.stereotype.Repository


@Repository
class TrainingPeaksActivityRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
) : ActivityRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun getActivities(startDate: LocalDate, endDate: LocalDate): List<Activity> {
        TODO("Not yet implemented")
    }

    override fun createActivity(activity: Activity) {
        val athleteId = trainingPeaksUserRepository.getUserId()
        val createRequest = CreateTPWorkoutDTO.createActivity(athleteId, activity)
        trainingPeaksApiClient.createAndPlanWorkout(athleteId, createRequest)
    }
}
