package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.config.PreviewOnly
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.ScheduleIssue
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.freekode.tp2intervals.rest.workout.WorkoutScheduledJobController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class WorkoutScheduledSafetyTest {
    private val service = mock(WorkoutService::class.java)
    private val schedules = mock(ScheduleRequestRepository::class.java)
    private val mapper = ObjectMapperFactory.objectMapper()
    private val today = LocalDate.of(2025, 2, 12)
    private val safe = C2CTodayScheduledRequest(listOf(TrainingType.BIKE), true, Platform.TRAINING_PEAKS, Platform.INTERVALS)
    private val reverse = safe.copy(
        skipSynced = false, sourcePlatform = Platform.INTERVALS, targetPlatform = Platform.TRAINING_PEAKS
    )

    @Test
    fun `audit and scheduler reject unsafe stored values while opted-in true and reverse jobs run`() {
        val good = mapper.writeValueAsString(safe)
        val rows = listOf(
            good.replace("\"skipSynced\":true", "\"skipSynced\":false"),
            good.replace("\"skipSynced\":true,", ""),
            good.replace("\"skipSynced\":true", "\"skipSynced\":null"),
            good.replace("\"skipSynced\":true", "\"skipSynced\":\"true\""),
            good.replace("\"skipSynced\":true", "\"skipSynced\":true,\"skipSynced\":false"),
            "broken",
            good + "{}",
            good,
            mapper.writeValueAsString(reverse),
        )
        `when`(schedules.findAll()).thenReturn(rows.mapIndexed { i, json -> ScheduleRequestEntity(i + 1, json) })
        val job = scheduler(enabled = true)

        val audit = job.auditRequests()
        assertEquals(
            listOf(
                ScheduleIssue.SKIP_SYNCED_FALSE, ScheduleIssue.SKIP_SYNCED_MISSING,
                ScheduleIssue.SKIP_SYNCED_MALFORMED, ScheduleIssue.SKIP_SYNCED_MALFORMED,
                ScheduleIssue.MALFORMED_REQUEST, ScheduleIssue.MALFORMED_REQUEST,
                ScheduleIssue.MALFORMED_REQUEST, null, null
            ),
            audit.map { it.blockedReason }
        )
        verifyNoInteractions(service)
        fixedToday { job.job() }

        verify(service).copyScheduledWorkouts(requestForToday(safe))
        verify(service).copyScheduledWorkouts(requestForToday(reverse))
        verifyNoMoreInteractions(service)
        verify(schedules, org.mockito.Mockito.times(2)).findAll()
        verifyNoMoreInteractions(schedules)
    }

    @Test
    fun `scheduled forward sync remains paused without the operator opt-in`() {
        `when`(schedules.findAll()).thenReturn(listOf(ScheduleRequestEntity(1, mapper.writeValueAsString(safe))))
        val job = scheduler(enabled = false)
        assertEquals(ScheduleIssue.FORWARD_SYNC_PAUSED, job.auditRequests().single().blockedReason)

        job.job()

        verifyNoInteractions(service)
    }

    @Test
    fun `schedule endpoint rejects new unsafe forward jobs without persisting them`() {
        val mvc = MockMvcBuilders.standaloneSetup(WorkoutScheduledJobController(scheduler(enabled = true))).build()
        val valid = mapper.writeValueAsString(safe)
        listOf(
            valid.replace("\"skipSynced\":true", "\"skipSynced\":false"),
            valid.replace("\"skipSynced\":true,", ""),
            valid.replace("\"skipSynced\":true", "\"skipSynced\":\"true\""),
        ).forEach { json ->
            mvc.perform(post("/api/workout/copy-calendar-to-calendar/schedule").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest)
        }

        verifyNoInteractions(service, schedules)
    }

    @Test
    fun `a safe forward request can be registered while forward scheduling remains paused`() {
        val mvc = MockMvcBuilders.standaloneSetup(WorkoutScheduledJobController(scheduler(enabled = false))).build()
        val json = mapper.writeValueAsString(safe)
        mvc.perform(post("/api/workout/copy-calendar-to-calendar/schedule").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk)

        verify(schedules).save(ScheduleRequestEntity(json))
        verifyNoInteractions(service)
    }

    private fun scheduler(enabled: Boolean) = WorkoutScheduledJob(service, schedules, mapper, enabled, PreviewOnly(false))

    private fun requestForToday(scheduled: C2CTodayScheduledRequest) =
        CopyFromCalendarToCalendarRequest(today, today, scheduled.types, scheduled.skipSynced, scheduled.sourcePlatform, scheduled.targetPlatform)

    private fun fixedToday(action: () -> Unit) {
        mockStatic(LocalDate::class.java, CALLS_REAL_METHODS).use { clock ->
            clock.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            action()
        }
    }
}
