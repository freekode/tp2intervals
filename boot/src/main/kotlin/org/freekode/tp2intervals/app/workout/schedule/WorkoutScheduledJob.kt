package org.freekode.tp2intervals.app.workout.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.app.workout.TrainerRoadSyncMode
import org.freekode.tp2intervals.app.workout.SkippedSyncWorkout
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduledSyncResult(
    val runAt: LocalDateTime,
    val failed: Boolean,
    val unidentifiedActivityDates: List<LocalDate> = emptyList(),
    val skippedWorkouts: List<SkippedSyncWorkout> = emptyList(),
)

@Service
class WorkoutScheduledJob(
    private val workoutService: WorkoutService,
    private val scheduleRequestRepository: ScheduleRequestRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun addRequest(schedulable: Schedulable) {
        if (schedulable is C2CTodayScheduledRequest) {
            validateScheduledIdentityPermission(schedulable)
            workoutService.validateCalendarRequest(schedulable.forToday())
        }
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
        val requests = getRequests()
        log.info("Starting processing scheduled requests. There are ${requests.size} requests")

        for (request in requests) {
            val result = try {
                val scheduled = request.toSchedulable()
                validateScheduledIdentityPermission(scheduled)
                val response = workoutService.copyWorkoutsC2C(scheduled.forToday())
                ScheduledSyncResult(LocalDateTime.now(), false, response.unidentifiedActivityDates, response.skippedWorkouts)
            } catch (e: Exception) {
                log.error("Failed to process scheduled request {}", request.id, e)
                ScheduledSyncResult(LocalDateTime.now(), true)
            }
            try {
                scheduleRequestRepository.updateLastResult(requireNotNull(request.id), objectMapper.writeValueAsString(result))
            } catch (e: Exception) {
                log.error("Failed to save scheduled request result {}", request.id, e)
            }
        }

        log.info("Finished processing scheduled requests");
    }

    private fun ScheduleRequestEntity.toSchedulable(): C2CTodayScheduledRequest {
        return objectMapper.readValue(requestJson, C2CTodayScheduledRequest::class.java)
    }

    private fun validateScheduledIdentityPermission(request: C2CTodayScheduledRequest) {
        require(!request.removeStaleTrainerRoad &&
            (request.trainerRoadSyncMode != TrainerRoadSyncMode.MIRROR ||
                (request.mirrorByTrainerRoadId && request.allowUnidentifiedSourceRemoval))) {
            "Scheduled removal uses an older policy; delete and recreate the job to accept the current Mirror policy"
        }
    }
}
