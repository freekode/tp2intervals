package org.freekode.tp2intervals.app.workout.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class WorkoutScheduledJob(
    private val workoutService: WorkoutService,
    private val scheduleRequestRepository: ScheduleRequestRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun addRequest(schedulable: Schedulable) {
        val requestJson = objectMapper.writeValueAsString(schedulable)
        if (scheduleRequestRepository.findByRequestJson(requestJson) != null) throw IllegalArgumentException("Request already exists")
        scheduleRequestRepository.save(ScheduleRequestEntity(requestJson))
    }

    fun getRequests() =
        scheduleRequestRepository.findAll().toList()

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

        log.info("Finished processing scheduled requests");
    }

    private fun handleCopyCalendarToCalendarRequest(request: C2CTodayScheduledRequest) {
        workoutService.copyWorkoutsC2C(request.forToday())
    }

    private fun ScheduleRequestEntity.toSchedulable(): C2CTodayScheduledRequest {
        return objectMapper.readValue(requestJson, C2CTodayScheduledRequest::class.java)
    }
}
