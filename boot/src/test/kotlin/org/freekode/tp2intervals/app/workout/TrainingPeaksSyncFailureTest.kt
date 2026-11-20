package org.freekode.tp2intervals.app.workout

import config.SyncLedgerFixture
import config.mock.ObjectMapperFactory
import feign.FeignException
import feign.Request
import feign.Response
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreatedIntervalsEvent
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.CreateEventRequestDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
import org.freekode.tp2intervals.rest.workout.WorkoutController
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import java.io.UncheckedIOException
import java.net.SocketTimeoutException
import java.time.Duration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicReference

class TrainingPeaksSyncFailureTest {
    @TempDir
    lateinit var tempDir: Path

    private val database by lazy { SyncLedgerFixture(tempDir.resolve("sync.sqlite")) }
    private val today = LocalDate.of(2025, 2, 12)
    private val manualEdit = event(1, "Edited by athlete", date = today.minusDays(1))
    private val intervals = StatefulIntervalsApi(manualEdit)
    private val schedule = C2CTodayScheduledRequest(
        listOf(TrainingType.BIKE), true, Platform.TRAINING_PEAKS, Platform.INTERVALS
    )
    private val objectMapper = ObjectMapperFactory.objectMapper()
    private val persistedRequest = ScheduleRequestEntity(1, objectMapper.writeValueAsString(schedule))
    private val key = Key(SOURCE_ACCOUNT, ATHLETE_ID, SOURCE_ID)

    @Test
    fun `independent scheduler instances can overlap for the same persisted request`() {
        val first = node()
        val second = node()
        val observer = database.newLedger()
        intervals.beforePost = {
            assertEquals(State.PENDING, observer.find(key)?.state, "claim committed before POST")
        }

        overlap({ runScheduled(first) }, { runScheduled(second) })

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
        assertEquals(State.CONFIRMED, observer.find(key)?.state)
        assertEquals(2L, observer.find(key)?.remoteEventId)
    }

    @Test
    fun `a single scheduler invocation processes persisted requests sequentially`() {
        val app = node(persistedRequest, persistedRequest.copy(id = 2))

        runScheduled(app)

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
    }

    @Test
    fun `manual request can overlap the scheduler in one instance`() {
        val app = node()
        val manualRequest = withToday { schedule.forToday() }

        val firstRead = CountDownLatch(1)
        val releaseRead = CountDownLatch(1)
        val manualThread = AtomicReference<Thread>()
        intervals.pauseNextRead = {
            firstRead.countDown()
            assertTrue(releaseRead.await(10, SECONDS))
        }
        val threads = Executors.newFixedThreadPool(2)
        try {
            val scheduled = threads.submit { runScheduled(app) }
            assertTrue(firstRead.await(5, SECONDS))
            val manual = threads.submit {
                manualThread.set(Thread.currentThread())
                app.controller.copyWorkoutsFromCalendarToCalendar(manualRequest)
            }
            // The scheduled read stays paused until the manual call queues on the lock.
            // Without the lock the manual call finishes against the same empty calendar.
            val deadline = System.nanoTime() + SECONDS.toNanos(5)
            while (!manual.isDone && manualThread.get()?.state != Thread.State.WAITING) {
                check(System.nanoTime() < deadline) { "Manual call neither completed nor queued" }
                Thread.yield()
            }
            releaseRead.countDown()
            scheduled.get(5, SECONDS)
            manual.get(5, SECONDS)
        } finally {
            releaseRead.countDown()
            threads.shutdownNow()
        }

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
    }

    @Test
    fun `lost POST response reconciles when the next GET sees the committed event`() {
        val app = node()
        intervals.loseNextPostResponse = true

        assertThrows(UncheckedIOException::class.java) { runScheduled(app) }
        assertEquals(State.PENDING, database.newLedger().find(key)?.state)
        runScheduled(app)

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
        assertEquals(2L, database.newLedger().find(key)?.remoteEventId)
    }

    @Test
    fun `lost POST response must not trigger a second POST when the commit is still hidden`() {
        val app = node()
        intervals.loseNextPostResponse = true
        intervals.hideNewEventsFromNextSnapshots = true

        assertThrows(UncheckedIOException::class.java) { runScheduled(app) }
        val review = runManual(app).syncReview.single()

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
        assertEquals(WorkoutSyncReview.Reason.UNRESOLVED_WRITE, review.reason)
        assertEquals(State.PENDING, database.newLedger().find(key)?.state)
    }

    @Test
    fun `successful POST must not repeat while the event is hidden from GET`() {
        val app = node()
        intervals.hideNewEventsFromNextSnapshots = true

        runScheduled(app)
        runScheduled(app)

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
        assertEquals(listOf(2L), intervals.lookedUpIds())
    }

    @Test
    fun `destination transport failure aborts before POST`() {
        val app = node()
        intervals.nextGetFailure = UncheckedIOException(SocketTimeoutException("GET timed out"))

        assertThrows(UncheckedIOException::class.java) { runScheduled(app) }

        assertCalendar(expectedSyncedEventId = null, expectedPosts = 0)
    }

    @Test
    fun `destination HTTP error aborts before POST`() {
        val app = node()
        val request = Request.create(Request.HttpMethod.GET, "/events", emptyMap(), null, Charsets.UTF_8, null)
        val response = Response.builder().request(request).status(503).reason("Unavailable").build()
        intervals.nextGetFailure = FeignException.errorStatus("getEvents", response)

        assertEquals(503, assertThrows(FeignException::class.java) { runScheduled(app) }.status())

        assertCalendar(expectedSyncedEventId = null, expectedPosts = 0)
    }

    @Test
    fun `partial destination result must not duplicate an omitted linked event`() {
        val app = node()
        val original = event(2, "Imported workout edited by athlete", SOURCE_ID)
        val visibleEvent = event(3, "Other planned workout")
        intervals.seed(original)
        intervals.seed(visibleEvent)
        intervals.omitEventOnNextGet = original.id

        runScheduled(app)

        assertCalendar(
            expectedSyncedEventId = original.id, expectedPosts = 0, original = original, visibleEvent = visibleEvent
        )
    }

    @Test
    fun `restarting after an uncertain POST must not create a second event`() {
        intervals.loseNextPostResponse = true
        intervals.hideNewEventsFromNextSnapshots = true

        assertThrows(UncheckedIOException::class.java) { runScheduled(node()) }
        // Recreate all application objects, keeping the SQLite file, stored request and fake remote calendar.
        val restarted = node()
        runScheduled(restarted)
        assertEquals(State.PENDING, database.newLedger().find(key)?.state)
        runScheduled(restarted)

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
        assertEquals(2L, database.newLedger().find(key)?.remoteEventId)
    }

    @Test
    fun `existing event moved out of the query range must be preserved without copying`() {
        val app = node()
        val original = event(2, "Rescheduled by athlete", SOURCE_ID, today.plusDays(1))
        intervals.seed(original)

        runScheduled(app)

        assertCalendar(expectedSyncedEventId = original.id, expectedPosts = 0, original = original)
    }

    @Test
    fun `legacy duplicates are flagged and never receive a POST`() {
        val app = node()
        val original = event(2, "Edited import", SOURCE_ID)
        val duplicate = event(3, "Moved duplicate", SOURCE_ID, today.plusDays(1))
        intervals.seed(original)
        intervals.seed(duplicate)

        repeat(2) {
            assertEquals(WorkoutSyncReview.Reason.AMBIGUOUS_IMPORT, runManual(app).syncReview.single().reason)
        }

        assertEquals(State.AMBIGUOUS, database.newLedger().find(key)?.state)
        val (events, posts) = intervals.state()
        assertEquals(listOf(manualEdit, original, duplicate), events)
        assertEquals(0, posts)
    }

    @Test
    fun `confirmed moved event is found by ID and its manual edits are preserved`() {
        val app = node()
        runScheduled(app)
        val created = intervals.state().first.last()
        val edited = IntervalsEventDTO(
            created.id, "Athlete edit", created.description, today.plusDays(50).atStartOfDay(),
            "WORKOUT", "Ride", 3600, null, null
        )
        intervals.replace(edited)

        assertEquals(emptyList<WorkoutSyncReview>(), runManual(app).syncReview)

        assertCalendar(expectedSyncedEventId = edited.id, expectedPosts = 1, original = edited)
        assertEquals(listOf(edited.id), intervals.lookedUpIds())
    }

    @Test
    fun `missing linked event is reported without recreating it`() {
        val app = node()
        assertEquals(true, database.newLedger().recordLegacyMatch(key, 42L))

        val review = runManual(app).syncReview.single()

        assertEquals(WorkoutSyncReview(SOURCE_ID, WorkoutSyncReview.Reason.LINKED_EVENT_MISSING, 42L), review)
        assertCalendar(expectedSyncedEventId = null, expectedPosts = 0)
        assertEquals(42L, database.newLedger().find(key)?.remoteEventId)
    }

    @Test
    fun `dismissed calendar 404 with an empty body list cannot authorize a POST`() {
        val app = node()
        intervals.snapshotStatus = HttpStatus.NOT_FOUND

        assertThrows(PlatformException::class.java) { runScheduled(app) }

        assertCalendar(expectedSyncedEventId = null, expectedPosts = 0)
    }

    @Test
    fun `create response without an ID remains pending`() {
        val app = node()
        intervals.hideNewEventsFromNextSnapshots = true
        intervals.omitNextPostBody = true

        assertThrows(PlatformException::class.java) { runScheduled(app) }
        assertEquals(WorkoutSyncReview.Reason.UNRESOLVED_WRITE, runManual(app).syncReview.single().reason)

        assertCalendar(expectedSyncedEventId = 2, expectedPosts = 1)
        assertEquals(State.PENDING, database.newLedger().find(key)?.state)
    }

    private fun runManual(app: App) = withToday {
        app.controller.copyWorkoutsFromCalendarToCalendar(schedule.forToday())
    }

    private fun node(vararg requests: ScheduleRequestEntity): App {
        val source = mock(TrainingPeaksWorkoutRepository::class.java)
        `when`(source.platform()).thenReturn(Platform.TRAINING_PEAKS)
        `when`(source.calendarAccountId()).thenReturn(SOURCE_ACCOUNT)
        `when`(source.getWorkoutsFromCalendar(SOURCE_ACCOUNT, today, today)).thenReturn(
            listOf(
                Workout(
                    WorkoutDetails(
                        TrainingType.BIKE, "Threshold", null, Duration.ofMinutes(70), null,
                        ExternalData.empty().withTrainingPeaks(SOURCE_ID)
                    ),
                    today,
                    null
                )
            )
        )
        val config = mock(IntervalsConfigurationRepository::class.java)
        `when`(config.getConfiguration()).thenReturn(IntervalsConfiguration("unused", ATHLETE_ID, 0.1f, 0.2f, 0.3f))
        val destination = IntervalsWorkoutRepository(intervals, config)
        val sync = TrainingPeaksCalendarSync(source, destination, database.newLedger())
        val service = WorkoutService(listOf(source, destination), emptyList(), sync)
        val schedules = mock(ScheduleRequestRepository::class.java)
        `when`(schedules.findAll()).thenReturn(requests.toList().ifEmpty { listOf(persistedRequest) })
        return App(WorkoutScheduledJob(service, schedules, objectMapper, scheduledForwardEnabled = true), WorkoutController(service))
    }

    private fun runScheduled(app: App) = withToday { app.job.job() }

    private fun <T> withToday(action: () -> T): T =
        mockStatic(LocalDate::class.java, CALLS_REAL_METHODS).use { clock ->
            clock.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            action()
        }

    private fun overlap(first: () -> Unit, second: () -> Unit) {
        intervals.snapshotBarrier = CyclicBarrier(2)
        val threads = Executors.newFixedThreadPool(2)
        try {
            val firstRun = threads.submit(first)
            val secondRun = threads.submit(second)
            firstRun.get(10, SECONDS)
            secondRun.get(10, SECONDS)
        } finally {
            threads.shutdownNow()
        }
    }

    private fun assertCalendar(
        expectedSyncedEventId: Long?,
        expectedPosts: Int,
        original: IntervalsEventDTO? = null,
        visibleEvent: IntervalsEventDTO? = null
    ) {
        val (events, posts) = intervals.state()
        val expectedEvents = listOfNotNull(
            manualEdit.id to null, expectedSyncedEventId?.let { it to SOURCE_ID }, visibleEvent?.let { it.id to null }
        )
        val identities = events.map { event ->
            val sourceId = ExternalData.empty().fromSimpleString(event.description.orEmpty()).trainingPeaksId
            event.id to sourceId
        }
        assertAll(
            { assertEquals(expectedEvents, identities, "remote event IDs and source IDs") },
            { assertEquals(expectedPosts, posts, "POST attempts") },
            { assertSame(manualEdit, events.firstOrNull { it.id == manualEdit.id }, "manual event preserved") },
            {
                listOfNotNull(original, visibleEvent).forEach { preserved ->
                    assertSame(preserved, events.firstOrNull { it.id == preserved.id }, "existing event preserved")
                }
            }
        )
    }

    private fun event(id: Long, name: String, sourceId: String? = null, date: LocalDate = today) =
        IntervalsEventDTO(
            id, name, sourceId?.let { "//////////\ntrainingPeaksId=$it" }, date.atStartOfDay(),
            "WORKOUT", "Ride", 4200, null, null
        )

    private data class App(val job: WorkoutScheduledJob, val controller: WorkoutController)

    private class StatefulIntervalsApi(manualEdit: IntervalsEventDTO) : IntervalsApiClient by mock(IntervalsApiClient::class.java) {
        private val events = mutableListOf(manualEdit)
        private var posts = 0
        private val lookedUpEventIds = mutableListOf<Long>()
        var beforePost: (() -> Unit)? = null
        var omitNextPostBody = false
        var snapshotStatus = HttpStatus.OK
        private var hiddenEventId: Long? = null
        private var remainingHiddenReads = 0
        var snapshotBarrier: CyclicBarrier? = null
        var pauseNextRead: (() -> Unit)? = null
        var loseNextPostResponse = false
        var hideNewEventsFromNextSnapshots = false
        var nextGetFailure: RuntimeException? = null
        var omitEventOnNextGet: Long? = null

        fun seed(event: IntervalsEventDTO) {
            events.add(event)
        }

        fun replace(event: IntervalsEventDTO) {
            events[events.indexOfFirst { it.id == event.id }] = event
        }

        fun state() = synchronized(this) { events.toList() to posts }

        fun lookedUpIds() = synchronized(this) { lookedUpEventIds.toList() }

        override fun getCalendarSnapshot(athleteId: String, oldest: String, newest: String) =
            ResponseEntity.status(snapshotStatus).body(getEvents(athleteId, oldest, newest, 0f, 0f, 0f))

        @Synchronized
        override fun getEvent(athleteId: String, eventId: Long): ResponseEntity<IntervalsEventDTO> {
            assertEquals(ATHLETE_ID, athleteId)
            lookedUpEventIds.add(eventId)
            return ResponseEntity.ofNullable(events.firstOrNull { it.id == eventId })
        }

        override fun getEvents(
            athleteId: String, startDate: String, endDate: String,
            powerRange: Float, hrRange: Float, paceRange: Float
        ): List<IntervalsEventDTO> {
            assertEquals(ATHLETE_ID, athleteId)
            val (snapshot, pause) = synchronized(this) {
                nextGetFailure?.let {
                    nextGetFailure = null
                    throw it
                }
                val omitted = omitEventOnNextGet
                val hidden = hiddenEventId
                omitEventOnNextGet = null
                if (remainingHiddenReads > 0 && --remainingHiddenReads == 0) hiddenEventId = null
                val pause = pauseNextRead
                pauseNextRead = null
                val snapshot = events.filter {
                    it.id != omitted && it.id != hidden && it.start_date_local.toLocalDate().toString() in startDate..endDate
                }
                snapshot to pause
            }
            pause?.invoke()
            // Take both snapshots before letting either caller proceed to POST.
            snapshotBarrier?.await(5, SECONDS)
            return snapshot
        }

        @Synchronized
        override fun createEvent(athleteId: String, createEventRequestDTO: CreateEventRequestDTO): ResponseEntity<CreatedIntervalsEvent> {
            assertEquals(ATHLETE_ID, athleteId)
            beforePost?.invoke()
            posts++
            val id = events.last().id + 1
            events.add(
                IntervalsEventDTO(
                    id, createEventRequestDTO.name, createEventRequestDTO.description,
                    LocalDateTime.parse(createEventRequestDTO.start_date_local),
                    createEventRequestDTO.category, createEventRequestDTO.type, 4200, null, null
                )
            )
            if (hideNewEventsFromNextSnapshots) {
                hiddenEventId = id
                remainingHiddenReads = 2
                hideNewEventsFromNextSnapshots = false
            }
            if (loseNextPostResponse) {
                loseNextPostResponse = false
                throw UncheckedIOException(SocketTimeoutException("POST response lost after commit"))
            }
            if (omitNextPostBody) {
                omitNextPostBody = false
                return ResponseEntity.ok().build()
            }
            return ResponseEntity.ok(CreatedIntervalsEvent(id))
        }
    }

    private companion object {
        const val ATHLETE_ID = "intervals-athlete"
        const val SOURCE_ID = "12345"
        const val SOURCE_ACCOUNT = "trainingpeaks-athlete"
    }
}
