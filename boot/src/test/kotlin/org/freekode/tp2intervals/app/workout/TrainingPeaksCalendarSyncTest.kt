package org.freekode.tp2intervals.app.workout

import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreatedIntervalsEvent
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUser
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPAttachmentService
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.time.LocalDateTime

class TrainingPeaksCalendarSyncTest {
    private val today = LocalDate.of(2025, 2, 12)
    private val objectMapper = ObjectMapperFactory.objectMapper()
    private val trainingPeaks = mock(TrainingPeaksApiClient::class.java)
    private val intervals = StatefulIntervalsApi()

    @Test
    fun `scheduled sync preserves an existing workout with edited fields`() {
        val original = existingEvent("12345", name = "Threshold (edited)")
        intervals.events.add(original)
        val job = scheduledJob("12345")

        runTwice(job)

        assertAll(
            { assertEquals(1, intervals.events.size, "destination event count") },
            { assertEquals(0, intervals.posts, "additional POSTs") },
            { assertSame(original, intervals.events.first(), "original event") },
        )
    }

    @Test
    fun `scheduled sync copies a distinct source ID with the same name and duration`() {
        // TrainingPeaks conversion truncates the planned duration to whole minutes.
        intervals.createdMovingTime = 70 * 60L
        val original = existingEvent("12345", movingTime = intervals.createdMovingTime)
        intervals.events.add(original)
        val job = scheduledJob("12345", "67890")

        runTwice(job)

        assertAll(
            { assertEquals(2, intervals.events.size, "one event per source ID") },
            { assertEquals(1, intervals.posts, "POST only the missing source ID") },
            { assertSame(original, intervals.events.first(), "original event") },
            { assertEquals(setOf("12345", "67890"), intervals.sourceIds()) },
        )
    }

    @Test
    fun `sequential replay matches an event returned after the first POST`() {
        val job = scheduledJob("12345")

        runTwice(job)

        assertAll(
            { assertEquals(1, intervals.events.size, "destination event count") },
            { assertEquals(1, intervals.posts, "only the first run should POST") },
            { assertEquals(setOf("12345"), intervals.sourceIds()) },
        )
    }

    @Test
    fun `scheduled sync with skip synced disabled still creates another copy`() {
        intervals.events.add(existingEvent("12345"))
        val job = scheduledJob("12345", skipSynced = false)

        runTwice(job)

        assertAll(
            { assertEquals(3, intervals.events.size) },
            { assertEquals(2, intervals.posts) },
            { assertEquals(0, intervals.reads) },
        )
    }

    private fun scheduledJob(vararg sourceIds: String, skipSynced: Boolean = true): WorkoutScheduledJob {
        val userId = "trainingpeaks-athlete"
        val users = mock(TrainingPeaksUserRepository::class.java)
        val attachments = mock(TPAttachmentService::class.java)
        `when`(users.getUser()).thenReturn(TrainingPeaksUser(userId, isAthlete = true, isPremium = false))
        `when`(trainingPeaks.getWorkouts(userId, today.toString(), today.toString())).thenReturn(
            sourceIds.map { id ->
                TPWorkoutCalendarResponseDTO(
                    today.atStartOfDay(), id, 2, "Threshold", WORKOUT_SECONDS / 3600.0,
                    null, null, null, null
                )
            }
        )
        `when`(trainingPeaks.getNotes(userId, today.toString(), today.plusDays(1).toString())).thenReturn(emptyList())
        sourceIds.forEach { id -> `when`(attachments.getAttachments(userId, id)).thenReturn(emptyList()) }

        val source = TrainingPeaksWorkoutRepository(
            trainingPeaks, mock(), TPToWorkoutConverter(), mock(), users, mock(), attachments, mock(), objectMapper
        )
        val configuration = mock(IntervalsConfigurationRepository::class.java)
        `when`(configuration.getConfiguration()).thenReturn(IntervalsConfiguration("unused", ATHLETE_ID, 0.1f, 0.2f, 0.3f))
        val destination = IntervalsWorkoutRepository(intervals, configuration)
        val service = WorkoutService(listOf(source, destination), emptyList())
        val schedules = mock(ScheduleRequestRepository::class.java)
        val request = C2CTodayScheduledRequest(listOf(TrainingType.BIKE), skipSynced, Platform.TRAINING_PEAKS, Platform.INTERVALS)
        `when`(schedules.findAll()).thenReturn(listOf(ScheduleRequestEntity(objectMapper.writeValueAsString(request))))
        return WorkoutScheduledJob(service, schedules, objectMapper)
    }

    private fun runTwice(job: WorkoutScheduledJob) {
        mockStatic(LocalDate::class.java, CALLS_REAL_METHODS).use { clock ->
            clock.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            job.job()
            job.job()
        }
    }

    private fun existingEvent(sourceId: String, name: String = "Threshold", movingTime: Long = WORKOUT_SECONDS) =
        IntervalsEventDTO(
            1L, name, "//////////\ntrainingPeaksId=$sourceId", today.atStartOfDay(),
            "WORKOUT", "Ride", movingTime, null, null
        )

    private class StatefulIntervalsApi : IntervalsApiClient by mock(IntervalsApiClient::class.java) {
        val events = mutableListOf<IntervalsEventDTO>()
        var createdMovingTime = WORKOUT_SECONDS
        var posts = 0
            private set
        var reads = 0
            private set

        override fun getEvents(
            athleteId: String,
            startDate: String,
            endDate: String,
            powerRange: Float,
            hrRange: Float,
            paceRange: Float
        ): List<IntervalsEventDTO> {
            assertEquals(ATHLETE_ID, athleteId)
            reads++
            return events.filter { it.start_date_local.toLocalDate().toString() in startDate..endDate }
        }

        override fun createEvent(athleteId: String, createEventRequestDTO: CreateEventRequestDTO): ResponseEntity<CreatedIntervalsEvent> {
            assertEquals(ATHLETE_ID, athleteId)
            posts++
            events.add(
                IntervalsEventDTO(
                    events.size + 1L, createEventRequestDTO.name, createEventRequestDTO.description,
                    LocalDateTime.parse(createEventRequestDTO.start_date_local), createEventRequestDTO.category,
                    createEventRequestDTO.type, createdMovingTime, null, null
                )
            )
            return ResponseEntity.ok(CreatedIntervalsEvent(events.last().id))
        }

        fun sourceIds() = events.mapNotNull { event ->
            event.description?.lineSequence()?.firstOrNull { it.startsWith("trainingPeaksId=") }
                ?.substringAfter("=")
        }.toSet()
    }

    private companion object {
        const val ATHLETE_ID = "intervals-athlete"
        const val WORKOUT_SECONDS = 70 * 60L + 25
    }
}
