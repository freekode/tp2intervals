package org.freekode.tp2intervals.app.workout.scheduled

import org.freekode.tp2intervals.app.workout.WorkoutService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class WorkoutJobScheduler(
    private val workoutService: WorkoutService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val scheduledRequests = mutableListOf<Schedulable>()

    fun addRequest(schedulable: Schedulable) =
        scheduledRequests.add(schedulable)

    fun getRequests() =
        scheduledRequests.toList()

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES)
    fun job() {
        val requests = getRequests()
        log.info("Starting processing scheduled requests. There are ${requests.size} requests")

        for (request in requests) {
            if (request is CopyFromCalendarToCalendarScheduledRequest) {
                handleCopyCalendarToCalendarRequest(request)
            }
        }

        log.info("Finished processing scheduled requests");
    }

    private fun handleCopyCalendarToCalendarRequest(request: CopyFromCalendarToCalendarScheduledRequest) {
        workoutService.copyWorkoutsFromCalendarToCalendar(request.forToday())
    }
}
