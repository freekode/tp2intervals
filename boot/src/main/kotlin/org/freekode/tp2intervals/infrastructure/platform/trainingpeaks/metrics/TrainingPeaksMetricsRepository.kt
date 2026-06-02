package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.metrics

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.wellness.Wellness
import org.freekode.tp2intervals.domain.wellness.WellnessRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.stereotype.Repository
import java.time.LocalDate


@CacheConfig(cacheNames = ["tpMetricsCache"])
@Repository
class TrainingPeaksMetricsRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
) : WellnessRepository {

    override fun platform() = Platform.TRAINING_PEAKS

    override fun getFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Wellness> {
        val athleteId = trainingPeaksUserRepository.getUser().userId
        val listMetricsDTO = trainingPeaksApiClient.getMetrics(
            athleteId,
            startDate.toString(),
            endDate.toString())

        val listWellness = listMetricsDTO.map {
            TPMetricsConverter(it).toDomain()
        }

        return listWellness
    }

    override fun saveToCalendar(wellnesses: List<Wellness?>, startDate: LocalDate, endDate: LocalDate) {
        val athleteId = trainingPeaksUserRepository.getUser().userId

        wellnesses.filterNotNull().forEach { wellness ->
            val requestDTO = TPMetricsConverter(wellness).toDTO(athleteId)
            if (requestDTO.getMetricWeight() == -1.0) {
                deleteWellness(athleteId, requestDTO)
            } else {
                trainingPeaksApiClient.createMetrics(athleteId, requestDTO)
            }
        }
    }

    private fun deleteWellness(athleteId: String, requestDTO: TPMetricsDTO) {
        val existingMetrics = trainingPeaksApiClient.getMetrics(
            athleteId,
            requestDTO.timeStamp.toString().take(10),
            requestDTO.timeStamp.toString().take(10)
        )

        existingMetrics.forEach { metric ->
            metric.getDetailByType(TrainingType.WEIGHT)?.let { weightDetail ->
                weightDetail.value = -1.0
                trainingPeaksApiClient.deleteMetrics(athleteId, metric)
            }
        }
    }

}
