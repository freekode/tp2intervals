package org.freekode.tp2intervals.infrastructure.platform.strava.activity

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.infrastructure.platform.strava.activity.StravaGetActivitiesResponseDTO.StravaActivityDTO
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class StravaActivityMapper {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

    fun toActivity(activityDTO: StravaActivityDTO): Activity {
        val offsetDateTime = OffsetDateTime.parse(activityDTO.startTime, dateTimeFormatter)
        val startedAt = offsetDateTime.toLocalDateTime()

        return Activity(
            startedAt,
            getType(activityDTO),
            activityDTO.name,
            null
        )
    }

    private fun getType(activityDTO: StravaActivityDTO) =
        when (activityDTO.sportType) {
            "Ride" -> TrainingType.BIKE
            "MountainBikeRide" -> TrainingType.MTB
            "VirtualRide" -> TrainingType.VIRTUAL_BIKE
            "Run" -> TrainingType.RUN
            "Swim" -> TrainingType.SWIM
            "WeightTraining" -> TrainingType.WEIGHT
            else -> TrainingType.UNKNOWN
        }

}
