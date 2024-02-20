package org.freekode.tp2intervals.infrastructure.platform.trainerroad.activity

import java.time.Duration
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.infrastructure.utils.Base64
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class TRActivityMapper {
    fun mapToActivity(dto: TrainerRoadActivityDTO, resource: Resource): Activity {
        val type = if (dto.CompletedRide.IsOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE

        return Activity(
            dto.CompletedRide.Date,
            type,
            dto.CompletedRide.Name,
            Duration.ofSeconds(dto.CompletedRide.Duration),
            dto.CompletedRide.Tss,
            Base64.toString(resource)
        )
    }
}
