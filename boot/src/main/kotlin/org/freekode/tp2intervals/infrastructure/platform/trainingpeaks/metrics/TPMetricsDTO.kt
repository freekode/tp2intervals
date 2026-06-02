package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.metrics

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPTrainingTypeMapper

class TPMetricsDTO(
    val id: String?,
    val athleteId: Long?,
    val timeStamp: String?, // Format: 2026-01-09T00:00:00
    val details: List<TPMetricsDetailDTO> = emptyList()
) {
    class TPMetricsDetailDTO(
        val parentId: Long?,
        val type: Int?,
        var value: Double?,
        val isPotentiallyNegative: Boolean?,
        val uploadClient: String?,
        val label: String?,
        val time: String?,
        val modifiedTime: String?
    )

    fun getMetricWeight(): Double? {
        return details.find { it.type == TPTrainingTypeMapper.getByType(TrainingType.WEIGHT) }?.value
    }

    fun getDetailByType(trainingType: TrainingType): TPMetricsDetailDTO? {
        val tpType = TPTrainingTypeMapper.getByType(trainingType)
        return details.find { it.type == tpType }
    }
}