package org.freekode.tp2intervals.app.workout.scheduled

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class WorkoutJobScheduler(
    private val objectMapper: ObjectMapper,
    private val workoutService: WorkoutService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val scheduledRequests = mutableSetOf<String>()

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val requests = getRequests()
        log.info("Starting processing scheduled requests. There are ${requests.size} requests")

        for (request in requests) {
            if (request is CopyFromCalendarToCalendarScheduledRequest) {
                workoutService.copyWorkoutsFromCalendarToCalendar(request.toRequest())
            }
        }

        log.info("Finished processing scheduled requests");
    }

    fun addRequest(schedulable: Schedulable) {
        val value = objectMapper.writeValueAsString(schedulable)
        scheduledRequests.add(value)
    }

    fun getRequests(): List<Schedulable> =
        scheduledRequests.map { objectMapper.readValue(it, Schedulable::class.java) }

}