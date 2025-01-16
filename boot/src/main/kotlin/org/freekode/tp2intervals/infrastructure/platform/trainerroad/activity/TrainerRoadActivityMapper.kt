package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.infrastructure.utils.Base64
import org.springframework.core.io.Resource

class TrainerRoadActivityMapper {
    fun mapToActivity(dto: TrainerRoadActivityDTO, resource: Resource): Activity {
        val type = if (dto.completedRide!!.IsOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE

        return Activity(
            dto.completedRide.Date,
            type,
            dto.completedRide.Name,
            Base64.encodeToString(resource)
        )
    }
}
