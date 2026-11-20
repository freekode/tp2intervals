package org.freekode.tp2intervals.app.workout

import config.SyncLedgerFixture
import config.mock.ObjectMapperFactory
import feign.RequestTemplate
import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.app.workout.schedule.ScheduleIssue
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.config.PreviewOnlyHttpGuard
import org.freekode.tp2intervals.config.PreviewOnlyProviderGuard
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.freekode.tp2intervals.rest.configuration.ConfigurationController
import org.freekode.tp2intervals.rest.workout.TrainingPeaksPreviewController
import org.freekode.tp2intervals.rest.workout.WorkoutController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Path

class PreviewOnlySafetyTest {
    @TempDir
    lateinit var tempDir: Path

    private val mode = PreviewOnly(true)
    private val mapper = ObjectMapperFactory.objectMapper()

    @Test
    fun `configuration exposes only public settings and credential presence`() {
        val service = mock(ConfigurationService::class.java)
        `when`(service.getConfigurations()).thenReturn(AppConfiguration(mapOf(
            "intervals.athlete-id" to "synthetic",
            "intervals.api-key" to "private-key",
            "training-peaks.auth-cookie" to "private-cookie",
            "unknown.secret" to "private-value",
        )))
        val mvc = MockMvcBuilders.standaloneSetup(ConfigurationController(service))
            .addFilters<org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder>(PreviewOnlyHttpGuard(mode)).build()
        val response = mvc.perform(get("/api/configuration")).andExpect(status().isOk)
            .andExpect(header().string("Cache-Control", "no-store")).andReturn().response.contentAsString
        val payload = mapper.readTree(response)
        assertEquals(setOf("intervals.athlete-id"), payload["config"].fieldNames().asSequence().toSet())
        assertEquals(true, payload["storedCredentials"]["intervals.api-key"].asBoolean())
        assertEquals(true, payload["storedCredentials"]["training-peaks.auth-cookie"].asBoolean())
        assertEquals(false, payload["storedCredentials"]["trainer-road.auth-cookie"].asBoolean())
    }

    @Test
    fun `preview-only HTTP guard blocks mutations and leaves preview authentication in force`() {
        val workout = mock(WorkoutService::class.java)
        val sync = mock(TrainingPeaksCalendarSync::class.java)
        val mvc = MockMvcBuilders.standaloneSetup(WorkoutController(workout), TrainingPeaksPreviewController(sync, "synthetic-token"))
            .setMessageConverters(MappingJackson2HttpMessageConverter(mapper))
            .addFilters<org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder>(PreviewOnlyHttpGuard(mode)).build()

        mvc.perform(post("/api/workout/copy-calendar-to-calendar")).andExpect(status().isForbidden)
        mvc.perform(post("/api/library-container/copy")).andExpect(status().isForbidden)
        mvc.perform(put("/api/configuration")).andExpect(status().isForbidden)
        mvc.perform(delete("/api/workout")).andExpect(status().isForbidden)
        mvc.perform(post("/actuator/loggers/root")).andExpect(status().isForbidden)
        mvc.perform(post("/api/workout/trainingpeaks-sync/preview").contentType(MediaType.APPLICATION_JSON)
            .content("""{"startDate":"2026-01-01","endDate":"2026-01-01"}""")).andExpect(status().isUnauthorized)
        verifyNoInteractions(workout, sync)
    }

    @Test
    fun `provider guard rejects non-read methods before transport`() {
        val guard = PreviewOnlyProviderGuard(mode)
        for (method in listOf("POST", "PUT", "PATCH", "DELETE")) {
            assertThrows(ResponseStatusException::class.java) { guard.apply(RequestTemplate().method(method)) }
        }
        guard.apply(RequestTemplate().method("GET"))
    }

    @Test
    fun `preview-only rejects every ledger mutation and any scheduled run including reverse`() {
        val db = SyncLedgerFixture(tempDir.resolve("sync.sqlite"))
        val seed = db.newLedger()
        val key = Key("synthetic", "synthetic", "1")
        seed.claim(key)
        val ledger = db.newLedger(mode = mode)
        val unknown = key.copy(sourceWorkoutId = "2")
        assertThrows(ResponseStatusException::class.java) { ledger.claim(unknown) }
        assertThrows(ResponseStatusException::class.java) { ledger.recordLegacyMatch(unknown, 5) }
        assertThrows(ResponseStatusException::class.java) { ledger.recordAmbiguousMatch(unknown) }
        assertThrows(ResponseStatusException::class.java) { ledger.confirm(key, 5) }
        assertEquals(State.PENDING, seed.find(key)?.state)
        assertEquals(null, seed.find(unknown))

        val service = mock(WorkoutService::class.java)
        val schedules = mock(ScheduleRequestRepository::class.java)
        val reverse = """{"types":["BIKE"],"skipSynced":false,"sourcePlatform":"INTERVALS","targetPlatform":"TRAINING_PEAKS"}"""
        `when`(schedules.findAll()).thenReturn(listOf(ScheduleRequestEntity(1, reverse)))
        val job = WorkoutScheduledJob(service, schedules, mapper, true, mode)
        assertEquals(ScheduleIssue.PREVIEW_ONLY, job.auditRequests().single().blockedReason)
        job.job()
        verifyNoInteractions(service)
    }
}
