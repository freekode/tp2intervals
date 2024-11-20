package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.activity.ActivityRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClientService
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class TrainerRoadActivityRepository(
    private val trUsernameRepository: TRUsernameRepository,
    private val trainerRoadApiClientService: TrainerRoadApiClientService,
) : ActivityRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun saveActivities(activities: List<Activity>) {
        TODO("Not yet implemented")
    }

    override fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity> {
        val username = trUsernameRepository.getUsername()
        return trainerRoadApiClientService.getActivities(username, startDate, endDate)
    }
}
