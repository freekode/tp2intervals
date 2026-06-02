package org.freekode.tp2intervals.app.wellness.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.app.wellness.WellnessService
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ScheduledJobService(
    private val wellnessService: WellnessService,
    private val scheduleRequestRepository: ScheduleRequestRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun addRequest(schedulable: Schedulable) {
        val requestJson = objectMapper.writeValueAsString(schedulable)
        if (scheduleRequestRepository.findByRequestJson(requestJson) != null) throw IllegalArgumentException("Request already exists")
        scheduleRequestRepository.save(ScheduleRequestEntity(requestJson))
    }

    fun getRequests(): List<ScheduleRequestEntity> {
        return scheduleRequestRepository.findAll().filter { record ->
            try {
                // Internal conversion just to check the types inside the JSON
                val request = objectMapper.readValue(record.requestJson, C2CTodayScheduledRequest::class.java)
                request.types.contains(TrainingType.WEIGHT)
            } catch (e: Exception) {
                // If JSON is incompatible, exclude it from the wellness list
                false
            }
        }
    }

    fun deleteRequest(id: Int) {
        scheduleRequestRepository.deleteById(id)
    }

    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.MINUTES)
    fun job() {
        val requests = getRequests().map { it.toSchedulable() }
        log.info("Starting processing scheduled requests. There are ${requests.size} requests")

        for (request in requests) {
            handleCopyCalendarToCalendarRequest(request)
        }

        log.info("Finished processing scheduled requests")
    }

    private fun handleCopyCalendarToCalendarRequest(request: C2CTodayScheduledRequest) {
        wellnessService.copyWellnessC2C(request.forToday())
    }

    private fun ScheduleRequestEntity.toSchedulable(): C2CTodayScheduledRequest {
        return objectMapper.readValue(requestJson, C2CTodayScheduledRequest::class.java)
    }
}