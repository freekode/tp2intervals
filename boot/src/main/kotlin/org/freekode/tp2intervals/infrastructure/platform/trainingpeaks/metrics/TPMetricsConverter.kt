package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.metrics

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.wellness.Wellness
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPTrainingTypeMapper

class TPMetricsConverter {

    private var metricsDTO: TPMetricsDTO? = null
    private var metrics: Wellness? = null

    // Constructor for when you have the DTO (coming from Trainingpeaks)
    constructor(metricsDTO: TPMetricsDTO) {
        this.metricsDTO = metricsDTO
    }

    // Constructor for when you have the Domain (to submit to Trainingpeaks)
    constructor(wellness: Wellness?) {
        this.metrics = wellness
    }

    fun toDTO(athleteId: String): TPMetricsDTO {
        val timeWithHour = "${metrics?.date}T00:00:00"
        val pType = TPTrainingTypeMapper.getByType(TrainingType.WEIGHT)

        return TPMetricsDTO(
            id = null,
            athleteId = athleteId.toLong(),
            timeStamp = "${metrics?.date}T00:00:00",
            details = listOf(
                TPMetricsDTO.TPMetricsDetailDTO(
                    parentId = null,
                    type = pType,
                    value = metrics?.weight,
                    isPotentiallyNegative = false,
                    uploadClient = null,
                    label = TrainingType.WEIGHT.toString(),
                    time = timeWithHour,
                    modifiedTime = null
                )
            )
        )
    }

    fun toDomain(): Wellness {
        return Wellness(
            date = metricsDTO?.timeStamp,
            type = TrainingType.WEIGHT.toString(),
            weight = metricsDTO?.getMetricWeight()
        )
    }

}