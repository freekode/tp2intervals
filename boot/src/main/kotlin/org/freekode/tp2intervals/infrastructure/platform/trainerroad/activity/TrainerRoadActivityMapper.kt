package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.infrastructure.utils.Base64
import org.springframework.core.io.Resource

class TrainerRoadActivityMapper {
    fun mapToActivity(dto: TrainerRoadActivityDTO, resource: Resource): Activity {
        val completedRide = dto.completedRide
        val type = if (completedRide?.IsOutside ?: (dto.isOutside == true)) {
            TrainingType.BIKE
        } else {
            TrainingType.VIRTUAL_BIKE
        }

        return Activity(
            completedRide?.Date ?: dto.date,
            type,
            completedRide?.Name ?: dto.name ?: "TrainerRoad activity",
            Base64.encodeToString(resource)
        )
    }
}
