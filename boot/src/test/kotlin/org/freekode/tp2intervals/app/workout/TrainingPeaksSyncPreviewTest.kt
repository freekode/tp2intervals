package org.freekode.tp2intervals.app.workout

import com.fasterxml.jackson.databind.SerializationFeature
import config.SyncLedgerFixture
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUser
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.rest.workout.TrainingPeaksPreviewController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class TrainingPeaksSyncPreviewTest {
    @TempDir
    lateinit var tempDir: Path

    private val dbPath get() = tempDir.resolve("preview.sqlite")
    private val database by lazy { SyncLedgerFixture(dbPath) }
    private val today = LocalDate.of(2025, 2, 12)
    private val tomorrow = today.plusDays(1)
    private val objectMapper = ObjectMapperFactory.objectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    private val trainingPeaks = mock(TrainingPeaksApiClient::class.java)
    private val intervals = mock(IntervalsApiClient::class.java)
    private val user = mock(TrainingPeaksUserRepository::class.java)
    private val calendarConfig = mock(IntervalsConfigurationRepository::class.java)

    @Test
    fun `authenticated preview classifies workouts without any provider or SQLite writes`() {
        val mvc = mvc()
        val writer = database.newLedger()
        val keys = (1..7).associateWith { Key(SOURCE, DESTINATION, it.toString()) }
        writer.recordLegacyMatch(keys.getValue(1), 101)
        writer.claim(keys.getValue(5))
        writer.recordLegacyMatch(keys.getValue(6), 106)
        writer.recordAmbiguousMatch(keys.getValue(7))
        val before = keys.mapValues { writer.find(it.value) }
        val beforeFile = Files.readAllBytes(dbPath)

        val moved = event(101, "1", today.plusDays(10))
        val single = event(102, "2", tomorrow, legacy = true)
        val first = event(103, "3", today, legacy = true)
        val second = event(113, "3", tomorrow, legacy = true)
        val pending = event(105, "5", tomorrow)
        `when`(trainingPeaks.getWorkouts(SOURCE, today.toString(), tomorrow.toString()))
            .thenReturn((1..7).map { workout(it.toString()) } + workout("outside", tomorrow.plusDays(1)))
        `when`(intervals.getCalendarSnapshot(DESTINATION, today.toString(), tomorrow.toString()))
            .thenReturn(ResponseEntity.ok(listOf(single, first, pending)))
        `when`(intervals.getCalendarSnapshot(DESTINATION, today.minusDays(365).toString(), tomorrow.plusDays(365).toString()))
            .thenReturn(ResponseEntity.ok(listOf(moved, single, first, second, pending)))
        listOf(moved, single, pending).forEach { event ->
            `when`(intervals.getEvent(DESTINATION, event.id)).thenReturn(ResponseEntity.ok(event))
        }
        `when`(intervals.getEvent(DESTINATION, 106)).thenReturn(ResponseEntity.notFound().build())

        val reply = fixedToday {
            mvc.perform(request("Bearer preview-test-token"))
                .andExpect(status().isOk)
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().response.contentAsString
        }
        val rows = objectMapper.readTree(reply).associateBy { it.path("sourceWorkoutId").asText() }
        val statuses = listOf(
            "VERIFIED_LINK", "SINGLE_LEGACY_CANDIDATE", "MULTIPLE_LEGACY_CANDIDATES",
            "NO_MATCH_OBSERVED", "UNRESOLVED_CLAIM", "BROKEN_OR_UNVERIFIED_LINK", "BROKEN_OR_UNVERIFIED_LINK"
        )
        assertEquals(statuses, rows.values.map { it.path("status").asText() })
        assertEquals(today.toString(), rows.getValue("1").path("sourceDate").asText())
        assertEquals(moved.start_date_local.toLocalDate().toString(), rows.getValue("1").path("candidates")[0].path("date").asText())
        assertEquals("LEGACY_ACCOUNT_UNVERIFIED", rows.getValue("2").path("reason").asText())
        assertEquals(listOf(103L, 113L), rows.getValue("3").path("candidates").map { it.path("eventId").asLong() })
        assertEquals("CLAIM_CANDIDATE_VERIFIED", rows.getValue("5").path("reason").asText())
        assertEquals(106L, rows.getValue("6").path("candidates")[0].path("eventId").asLong())
        assertEquals("LINK_MISSING", rows.getValue("6").path("reason").asText())
        assertFalse(reply.contains("Edited athlete workout"))
        assertEquals(before, keys.mapValues { writer.find(it.value) })
        org.junit.jupiter.api.Assertions.assertArrayEquals(beforeFile, Files.readAllBytes(dbPath))

        verify(trainingPeaks).getWorkouts(SOURCE, today.toString(), tomorrow.toString())
        verifyNoMoreInteractions(trainingPeaks)
        verify(intervals).getCalendarSnapshot(DESTINATION, today.toString(), tomorrow.toString())
        verify(intervals).getCalendarSnapshot(DESTINATION, today.minusDays(365).toString(), tomorrow.plusDays(365).toString())
        listOf(101L, 102L, 105L, 106L).forEach { verify(intervals).getEvent(DESTINATION, it) }
        verifyNoMoreInteractions(intervals)
    }

    @Test
    fun `missing invalid and disabled credentials prevent provider access`() {
        val mvc = mvc()
        mvc.perform(request(null)).andExpect(status().isUnauthorized)
        mvc.perform(request("Bearer wrong")).andExpect(status().isUnauthorized)
        mvc("").perform(request("Bearer preview-test-token")).andExpect(status().isServiceUnavailable)
        verifyNoInteractions(user, calendarConfig, trainingPeaks, intervals)
    }

    @Test
    fun `Basic account range outside today and tomorrow is rejected before calendar requests`() {
        val mvc = mvc()
        fixedToday {
            for ((start, end) in listOf(today.minusDays(1) to today, tomorrow to tomorrow.plusDays(1))) {
                mvc.perform(request("Bearer preview-test-token", start, end))
                    .andExpect(status().isBadRequest)
                    .andExpect(status().reason("BASIC_TODAY_OR_TOMORROW_ONLY"))
            }
        }
        verifyNoInteractions(calendarConfig, trainingPeaks, intervals)
    }

    private fun mvc(token: String = "preview-test-token"): org.springframework.test.web.servlet.MockMvc {
        `when`(user.getUser()).thenReturn(TrainingPeaksUser(SOURCE, isAthlete = true, isPremium = false))
        `when`(calendarConfig.getConfiguration()).thenReturn(IntervalsConfiguration("unused", DESTINATION, 0f, 0f, 0f))
        val source = TrainingPeaksWorkoutRepository(
            trainingPeaks, mock(), TPToWorkoutConverter(), mock(), user, mock(), mock(), mock(), objectMapper
        )
        val io = IntervalsWorkoutRepository(intervals, calendarConfig)
        val controller = TrainingPeaksPreviewController(TrainingPeaksCalendarSync(source, io, database.newLedger(readOnly = true)), token)
        return MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    private fun request(authorization: String?, start: LocalDate = today, end: LocalDate = tomorrow) =
        post("/api/workout/trainingpeaks-sync/preview").contentType(MediaType.APPLICATION_JSON)
            .content("""{"startDate":"$start","endDate":"$end","types":["BIKE"]}""")
            .apply { if (authorization != null) header("Authorization", authorization) }

    private fun <T> fixedToday(block: () -> T): T =
        mockStatic(LocalDate::class.java, CALLS_REAL_METHODS).use { clock ->
            clock.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            block()
        }

    private fun workout(id: String, date: LocalDate = today) =
        TPWorkoutCalendarResponseDTO(date.atStartOfDay(), id, 2, "Synthetic", 1.0, null, null, null, null)

    private fun event(id: Long, workoutId: String, date: LocalDate, legacy: Boolean = false): IntervalsEventDTO {
        val account = if (legacy) "" else "\ntrainingPeaksAccountId=$SOURCE"
        return IntervalsEventDTO(
            id, "Edited athlete workout", "//////////\ntrainingPeaksId=$workoutId$account",
            date.atStartOfDay(), "WORKOUT", "Ride", null, null, null
        )
    }

    private companion object {
        const val SOURCE = "trainingpeaks-synthetic"
        const val DESTINATION = "intervals-synthetic"
    }
}
