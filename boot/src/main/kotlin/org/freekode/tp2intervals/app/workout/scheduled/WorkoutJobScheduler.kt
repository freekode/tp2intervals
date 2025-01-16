package org.freekode.tp2intervals.app.workout.scheduled

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
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
    private val scheduledRequests = mutableSetOf<Schedulable>()

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val requests = getRequests()
        log.info("Starting processing scheduled requests. There are ${requests.size} requests")

        for (request in requests) {
            if (request is CopyFromCalendarToCalendarRequest) {
                handleCopyCalendartoCalendarRequest(request)
            }
        }

        log.info("Finished processing scheduled requests");
    }

    private fun handleCopyCalendartoCalendarRequest(request: CopyFromCalendarToCalendarRequest) {
        workoutService.copyWorkoutsFromCalendarToCalendar(request.forToday())
    }

    fun addRequest(schedulable: Schedulable) {
        scheduledRequests.add(schedulable)
    }

    fun getRequests(): List<Schedulable> =
        scheduledRequests.toList()

}