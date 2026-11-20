package org.freekode.tp2intervals.app.workout.schedule

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.config.PreviewOnly
import org.springframework.beans.factory.annotation.Value
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
    private val objectMapper: ObjectMapper,
    @Value("\${app.training-peaks.scheduled-forward-sync-enabled:false}")
    private val scheduledForwardEnabled: Boolean,
    private val mode: PreviewOnly,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val scheduleReader = objectMapper.reader()
        .with(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
        .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)

    fun addRequest(requestJson: String) {
        val (request, issue) = inspect(requestJson)
        requireNotNull(request) { issue!!.name }
        addRequest(request)
    }

    fun addRequest(schedulable: C2CTodayScheduledRequest) {
        mode.requireWritable()
        require(!isForward(schedulable) || schedulable.skipSynced) { ScheduleIssue.SKIP_SYNCED_FALSE.name }
        val requestJson = objectMapper.writeValueAsString(schedulable)
        if (scheduleRequestRepository.findByRequestJson(requestJson) != null) throw IllegalArgumentException("Request already exists")
        scheduleRequestRepository.save(ScheduleRequestEntity(requestJson))
    }

    fun getRequests() =
        scheduleRequestRepository.findAll().toList()

    fun deleteRequest(id: Int) {
        mode.requireWritable()
        scheduleRequestRepository.deleteById(id)
    }

    fun auditRequests(): List<AuditedScheduleRequest> = getRequests().map { entry ->
        val (request, issue) = inspect(entry.requestJson)
        AuditedScheduleRequest(entry.id, request, blockedReason(request, issue))
    }

    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.MINUTES)
    fun job() {
        for (entry in getRequests()) {
            val (request, issue) = inspect(entry.requestJson)
            val blocked = blockedReason(request, issue)
            if (blocked != null) {
                log.warn("Scheduled sync {} blocked: {}", entry.id, blocked)
                continue
            }
            workoutService.copyWorkoutsC2C(checkNotNull(request).forToday())
        }
    }

    private fun inspect(requestJson: String?): Pair<C2CTodayScheduledRequest?, ScheduleIssue?> {
        if (requestJson.isNullOrBlank()) return null to ScheduleIssue.MALFORMED_REQUEST
        val json: JsonNode = try {
            scheduleReader.readTree(requestJson)
        } catch (_: JsonProcessingException) {
            return null to ScheduleIssue.MALFORMED_REQUEST
        }
        if (!json.isObject) return null to ScheduleIssue.MALFORMED_REQUEST
        val skipSynced = json.get("skipSynced") ?: return null to ScheduleIssue.SKIP_SYNCED_MISSING
        if (!skipSynced.isBoolean) return null to ScheduleIssue.SKIP_SYNCED_MALFORMED
        val request = try {
            objectMapper.treeToValue(json, C2CTodayScheduledRequest::class.java)
        } catch (_: JsonProcessingException) {
            return null to ScheduleIssue.MALFORMED_REQUEST
        }
        return request to if (isForward(request) && !request.skipSynced) ScheduleIssue.SKIP_SYNCED_FALSE else null
    }

    private fun blockedReason(request: C2CTodayScheduledRequest?, issue: ScheduleIssue?): ScheduleIssue? =
        issue ?: if (mode.enabled) ScheduleIssue.PREVIEW_ONLY
        else if (request != null && isForward(request) && !scheduledForwardEnabled) {
            ScheduleIssue.FORWARD_SYNC_PAUSED
        } else null

    private fun isForward(request: C2CTodayScheduledRequest) =
        request.sourcePlatform == Platform.TRAINING_PEAKS && request.targetPlatform == Platform.INTERVALS
}

data class AuditedScheduleRequest(val id: Int?, val request: C2CTodayScheduledRequest?, val blockedReason: ScheduleIssue?)

enum class ScheduleIssue {
    PREVIEW_ONLY,
    SKIP_SYNCED_FALSE,
    SKIP_SYNCED_MISSING,
    SKIP_SYNCED_MALFORMED,
    MALFORMED_REQUEST,
    FORWARD_SYNC_PAUSED,
}
