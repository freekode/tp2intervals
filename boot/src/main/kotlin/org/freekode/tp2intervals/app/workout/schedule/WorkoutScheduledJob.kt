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
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import feign.FeignException
import org.freekode.tp2intervals.infrastructure.ProviderReadFailure

@Service
class WorkoutScheduledJob(
    private val workoutService: WorkoutService,
    private val scheduleRequestRepository: ScheduleRequestRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${app.training-peaks.scheduled-forward-sync-enabled:false}")
    private val scheduledForwardEnabled: Boolean,
    private val mode: PreviewOnly,
    private val forwardSettings: ForwardScheduleSettings = ForwardScheduleSettings(),
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private var nextForward = Instant.MIN
    private var nextOther = Instant.MIN

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
        require(isForward(schedulable) || schedulable.window == null) { ScheduleIssue.UNSUPPORTED_WINDOW.name }
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

    fun settings() = ScheduleSettings(
        forwardSettings.intervalMinutes, forwardSettings.timezone, scheduledForwardEnabled, mode.enabled,
        OTHER_INTERVAL_MINUTES, ZoneId.systemDefault().id,
    )

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    fun job() = runDue(forwardSettings.now())

    @Synchronized
    internal fun runDue(now: Instant) {
        val forwardDue = now >= nextForward
        val otherDue = now >= nextOther
        if (!forwardDue && !otherDue) return
        if (forwardDue) nextForward = now.plus(forwardSettings.intervalMinutes, ChronoUnit.MINUTES)
        if (otherDue) nextOther = now.plus(OTHER_INTERVAL_MINUTES, ChronoUnit.MINUTES)
        val entries = try {
            getRequests()
        } catch (error: RuntimeException) {
            log.warn("Scheduled sync scan failed exception={}", error.javaClass.simpleName)
            return
        }

        for (entry in entries) {
            val (request, issue) = inspect(entry.requestJson)
            val forward = request?.let(::isForward) ?: false
            if ((forward && !forwardDue) || (!forward && !otherDue)) continue
            val blocked = blockedReason(request, issue)
            if (blocked != null) {
                log.info("Scheduled sync blocked id={} source={} target={} code={}",
                    entry.id, request?.sourcePlatform, request?.targetPlatform, blocked)
                continue
            }
            val scheduled = checkNotNull(request)
            val today = if (forward) forwardSettings.today(now) else now.atZone(ZoneId.systemDefault()).toLocalDate()
            val copy = scheduled.forToday(today)
            log.info("Scheduled sync started id={} source={} target={} start={} end={}",
                entry.id, copy.sourcePlatform, copy.targetPlatform, copy.startDate, copy.endDate)
            try {
                val outcome = workoutService.copyScheduledWorkouts(copy, today)
                val result = outcome.result
                log.info("Scheduled sync finished id={} source={} target={} start={} end={} blocked={} copied={} skipped={}",
                    entry.id, copy.sourcePlatform, copy.targetPlatform, copy.startDate, copy.endDate,
                    outcome.blockedReason, result?.copied, result?.filteredOut)
            } catch (error: RuntimeException) {
                val status = (error as? FeignException)?.status()?.takeIf { it > 0 }
                    ?: (error as? ProviderReadFailure)?.upstreamStatus
                log.warn("Scheduled sync failed id={} source={} target={} start={} end={} exception={} upstreamStatus={}",
                    entry.id, copy.sourcePlatform, copy.targetPlatform, copy.startDate, copy.endDate,
                    error.javaClass.simpleName, status)
            }
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
        if (json.has("window") && (!json["window"].isTextual ||
                json["window"].textValue() != ScheduleWindow.TODAY_AND_TOMORROW.name)
        ) return null to ScheduleIssue.UNSUPPORTED_WINDOW
        val request = try {
            objectMapper.treeToValue(json, C2CTodayScheduledRequest::class.java)
        } catch (_: JsonProcessingException) {
            return null to ScheduleIssue.MALFORMED_REQUEST
        }
        val issue = when {
            isForward(request) && !request.skipSynced -> ScheduleIssue.SKIP_SYNCED_FALSE
            !isForward(request) && request.window != null -> ScheduleIssue.UNSUPPORTED_WINDOW
            else -> null
        }
        return request to issue
    }

    private fun blockedReason(request: C2CTodayScheduledRequest?, issue: ScheduleIssue?): ScheduleIssue? =
        issue ?: if (mode.enabled) ScheduleIssue.PREVIEW_ONLY
        else if (request != null && isForward(request) && !scheduledForwardEnabled) {
            ScheduleIssue.FORWARD_SYNC_PAUSED
        } else null

    private companion object {
        const val OTHER_INTERVAL_MINUTES = 3L
    }

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
    UNSUPPORTED_WINDOW,
}


data class ScheduleSettings(
    val intervalMinutes: Long,
    val timezone: String,
    val forwardEnabled: Boolean,
    val previewOnly: Boolean,
    val otherIntervalMinutes: Long,
    val otherTimezone: String,
)
