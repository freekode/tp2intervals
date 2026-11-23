package org.freekode.tp2intervals.app.workout

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import config.mock.ObjectMapperFactory
import feign.FeignException
import feign.Request
import feign.Response
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.ForwardScheduleSettings
import org.freekode.tp2intervals.app.workout.schedule.ScheduleIssue
import org.freekode.tp2intervals.app.workout.schedule.ScheduleWindow
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

class RollingWorkoutScheduleTest {
    private val mapper = ObjectMapperFactory.objectMapper()
    private val service = mock(WorkoutService::class.java)
    private val repo = mock(ScheduleRequestRepository::class.java)
    private val legacy = C2CTodayScheduledRequest(listOf(TrainingType.BIKE), true, Platform.TRAINING_PEAKS, Platform.INTERVALS)
    private val rolling = legacy.copy(window = ScheduleWindow.TODAY_AND_TOMORROW)
    private val reverse = legacy.copy(sourcePlatform = Platform.INTERVALS, targetPlatform = Platform.TRAINING_PEAKS)
    private val settings = ForwardScheduleSettings(20, "Europe/Amsterdam")

    @Test
    fun `operational settings validate cadence and IANA zone`() {
        assertEquals(60L, ForwardScheduleSettings().intervalMinutes)
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            assertEquals("America/Los_Angeles", ForwardScheduleSettings().timezone)
        } finally {
            TimeZone.setDefault(original)
        }
        assertEquals(30L, ForwardScheduleSettings(30).intervalMinutes)
        for (minutes in listOf(0L, 4L, 1441L)) assertThrows(IllegalArgumentException::class.java) {
            ForwardScheduleSettings(minutes)
        }
        for (zone in listOf("+02:00", "Amsterdam")) assertThrows(IllegalArgumentException::class.java) {
            ForwardScheduleSettings(timezone = zone)
        }
    }

    @Test
    fun `one scan respects independent cadence and legacy JSON survives restart`() {
        rows(rolling, legacy, reverse)
        val job = job()
        val first = Instant.parse("2026-11-22T22:50:00Z")
        val before = LocalDate.parse("2026-11-22")
        val after = before.plusDays(1)
        assertFalse(mapper.writeValueAsString(legacy).contains("window"))
        assertEquals(null, job.auditRequests()[1].request?.window)
        job.runDue(first)
        job.runDue(first.plusSeconds(180))
        job.runDue(first.plusSeconds(1140))
        job.runDue(first.plusSeconds(1200))
        verify(service).copyScheduledWorkouts(rolling.forToday(before), before)
        verify(service).copyScheduledWorkouts(legacy.forToday(before), before)
        listOf(first, first.plusSeconds(180), first.plusSeconds(1140))
            .groupingBy { it.atZone(ZoneId.systemDefault()).toLocalDate() }.eachCount()
            .forEach { (date, count) -> verify(service, times(count)).copyScheduledWorkouts(reverse.forToday(date), date) }
        verify(service).copyScheduledWorkouts(rolling.forToday(after), after)
        verify(service).copyScheduledWorkouts(legacy.forToday(after), after)

        job().runDue(first.plusSeconds(1201))
        verify(service, times(2)).copyScheduledWorkouts(rolling.forToday(after), after)
        verify(service, times(2)).copyScheduledWorkouts(legacy.forToday(after), after)
    }

    @Test
    fun `Amsterdam midnight and both DST changes retain a two calendar day window`() {
        for ((instant, date) in listOf(
            "2026-03-28T22:59:00Z" to "2026-03-28",
            "2026-03-28T23:00:00Z" to "2026-03-29",
            "2026-03-29T00:59:00Z" to "2026-03-29",
            "2026-03-29T01:00:00Z" to "2026-03-29",
            "2026-10-25T00:59:00Z" to "2026-10-25",
            "2026-10-25T01:00:00Z" to "2026-10-25",
            "2026-10-25T23:00:00Z" to "2026-10-26",
        )) {
            clearInvocations(service)
            rows(rolling)
            val expected = LocalDate.parse(date)
            val clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)
            val calendarSettings = ForwardScheduleSettings(20, "Europe/Amsterdam", clock)
            assertEquals(expected, calendarSettings.today())
            WorkoutScheduledJob(service, repo, mapper, true, PreviewOnly(false), calendarSettings).job()
            verify(service).copyScheduledWorkouts(rolling.forToday(expected), expected)
        }
    }

    @Test
    fun `paused rolling jobs persist but both gates block dispatch`() {
        val saved = mapper.writeValueAsString(rolling)
        val paused = job(enabled = false)
        paused.addRequest(saved)
        verify(repo).save(ScheduleRequestEntity(saved))
        rows(rolling, reverse)
        assertEquals(ScheduleIssue.FORWARD_SYNC_PAUSED, paused.auditRequests().first().blockedReason)
        val instant = Instant.parse("2026-11-22T12:00:00Z")
        paused.runDue(instant)
        val otherDay = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        verify(service).copyScheduledWorkouts(reverse.forToday(otherDay), otherDay)
        clearInvocations(service)
        job(previewOnly = true).runDue(instant)
        verifyNoInteractions(service)
        assertThrows(IllegalArgumentException::class.java) { paused.addRequest(reverse.copy(window = rolling.window)) }
        assertThrows(IllegalArgumentException::class.java) { paused.addRequest(saved.replace("TODAY_AND_TOMORROW", "UNKNOWN")) }
    }

    @Test
    fun `a provider boundary error is redacted and later requests still execute`() {
        rows(rolling, legacy)
        val today = LocalDate.parse("2026-11-22")
        val secret = "synthetic-private-body-cookie-token"
        val request = Request.create(Request.HttpMethod.GET, "https://synthetic.invalid/?secret=$secret",
            emptyMap(), null, Charsets.UTF_8, null)
        val response = Response.builder().request(request).status(403).reason(secret).body(secret, Charsets.UTF_8).build()
        doThrow(FeignException.errorStatus("synthetic", response))
            .`when`(service).copyScheduledWorkouts(rolling.forToday(today), today)
        val logger = LoggerFactory.getLogger(WorkoutScheduledJob::class.java) as Logger
        val events = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(events)
        try {
            job().runDue(Instant.parse("2026-11-22T12:00:00Z"))
            verify(service).copyScheduledWorkouts(legacy.forToday(today), today)
            val messages = events.list.joinToString("\n") { it.formattedMessage }
            assertTrue("Scheduled sync failed id=1" in messages && "upstreamStatus=403" in messages)
            assertTrue("Scheduled sync finished id=2" in messages && "copied=0 filteredOut=1" in messages)
            assertFalse(secret in messages)
            assertTrue(events.list.all { it.throwableProxy == null })
        } finally {
            logger.detachAppender(events)
            events.stop()
        }
    }

    private fun rows(vararg requests: C2CTodayScheduledRequest) {
        `when`(repo.findAll()).thenReturn(requests.mapIndexed { index, request ->
            ScheduleRequestEntity(index + 1, mapper.writeValueAsString(request))
        })
        doAnswer {
            val request = it.getArgument<CopyFromCalendarToCalendarRequest>(0)
            ScheduledCopyOutcome(CopyWorkoutsResponse(0, 1, request.startDate, request.endDate, ExternalData.empty()))
        }.`when`(service).copyScheduledWorkouts(anyRequest(), anyDate())
    }

    private fun anyRequest(): CopyFromCalendarToCalendarRequest =
        org.mockito.ArgumentMatchers.any(CopyFromCalendarToCalendarRequest::class.java) ?: legacy.forToday()

    private fun anyDate(): LocalDate =
        org.mockito.ArgumentMatchers.any(LocalDate::class.java) ?: LocalDate.of(2026, 1, 1)

    private fun job(enabled: Boolean = true, previewOnly: Boolean = false) =
        WorkoutScheduledJob(service, repo, mapper, enabled, PreviewOnly(previewOnly), settings)
}
