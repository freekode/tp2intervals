package org.freekode.tp2intervals.app.workout

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import config.SyncLedgerFixture
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository.CalendarIdentity
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsCalendarEventDTO
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUser
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksLegacyBaselineRepository.Accounts
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.Key
import org.freekode.tp2intervals.infrastructure.sync.TrainingPeaksSyncLedger.State
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Path
import java.time.LocalDate
import org.freekode.tp2intervals.rest.workout.WorkoutController
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TrainingPeaksLegacyBaselineTest {
    @TempDir
    lateinit var temp: Path

    private val db by lazy { SyncLedgerFixture(temp.resolve("baseline.sqlite")) }
    private val day = LocalDate.of(2025, 2, 12)
    private val dates = TrainingPeaksPreviewRequest(day, day, listOf(TrainingType.BIKE))
    private val source = mock(TrainingPeaksWorkoutRepository::class.java)
    private val destination = mock(IntervalsWorkoutRepository::class.java)
    private val target = CalendarIdentity("iCanonical", "Synthetic")
    private val mapper = ObjectMapperFactory.objectMapper()

    @Test
    fun `direct REST forward force copy is rejected before calling either provider and reverse remains supported`() {
        doReturn(Platform.TRAINING_PEAKS).`when`(source).platform()
        doReturn(Platform.INTERVALS).`when`(destination).platform()
        val service = WorkoutService(listOf(source, destination), emptyList(), sync())
        val mvc = MockMvcBuilders.standaloneSetup(WorkoutController(service))
            .setMessageConverters(MappingJackson2HttpMessageConverter(mapper)).build()
        clearInvocations(source, destination)
        val forward = request(day).copy(skipSynced = false)
        mvc.perform(post("/api/workout/copy-calendar-to-calendar").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(forward)))
            .andExpect(status().isBadRequest)
            .andExpect(status().reason("SKIP_SYNCED_REQUIRED_FOR_FORWARD_COPY"))
        verifyNoInteractions(source, destination)

        val reverse = forward.copy(sourcePlatform = Platform.INTERVALS, targetPlatform = Platform.TRAINING_PEAKS)
        val imported = workout("reverse", day)
        doReturn(listOf(imported)).`when`(destination).getWorkoutsFromCalendar(day, day)
        mvc.perform(post("/api/workout/copy-calendar-to-calendar").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(reverse))).andExpect(status().isOk)
        verify(source).saveWorkoutsToCalendar(listOf(imported))
    }

    @Test
    fun `an enabled writer without approval cannot read or post and a paused approved writer cannot post`() {
        sync(enabled = true).copyScheduled(request(day), day)
        verifyNoInteractions(source, destination)

        db.seedBaseline("source", target.athleteId)
        sync(enabled = false).copyScheduled(request(day), day)
        sync(enabled = true).copyScheduled(request(day).copy(skipSynced = false), day)
        sync(enabled = true, previewOnly = true).copyScheduled(request(day), day)
        verifyNoInteractions(source, destination)
    }

    @Test
    fun `approved baseline survives restart midnight and pause while a newly encountered ID posts once`() {
        stub(day, listOf("old"))
        val review = sync()
        val baseline = approve(review)
        assertNotNull(baseline.approvedAt)
        assertTrue(db.baselines().isHeld(key("old")))

        val tomorrow = day.plusDays(1)
        stub(tomorrow, listOf("old", "new"))
        val newWorkout = workout("new", tomorrow)
        doReturn(71L).`when`(destination).createScopedCalendarEvent(target, "source", newWorkout)

        sync(enabled = false).copyScheduled(request(tomorrow), tomorrow)
        sync(enabled = true).copyScheduled(request(tomorrow), tomorrow)
        verify(destination).createScopedCalendarEvent(target, "source", newWorkout)
        assertNull(db.newLedger().find(key("old")))
        assertEquals(71L, db.newLedger().find(key("new"))?.remoteEventId)
        assertEquals("APPROVED", db.baselines().latest()?.state)
        assertEquals(baseline.revision, db.baselines().latest()?.revision)
    }

    @Test
    fun `provider identity switch invalidates approval even if the previous account returns`() {
        stub(day, emptyList())
        approve(sync())
        doReturn(TrainingPeaksUser("different", true, false)).`when`(source).calendarUser()
        sync(enabled = true).copyScheduled(request(day), day)
        assertEquals("INVALIDATED", db.baselines().latest()?.state)

        stub(day, listOf("new"))
        sync(enabled = true).copyScheduled(request(day), day)
        assertNull(db.newLedger().find(key("new")))
    }

    @Test
    fun `changed configuration invalidates approval and rejects an outstanding claim authorization across connections`() {
        db.seedBaseline("source", target.athleteId)
        val approval = db.baselines().latest()!!
        db.jdbc().update("INSERT INTO config(key,value) VALUES ('intervals.athlete-id','other')")
        assertFalse(db.newLedger().claimForSync(key("new"), approval))
        assertEquals("INVALIDATED", db.baselines().latest()?.state)
        assertEquals(approval.accounts.generation + 1, db.baselines().generation())
    }

    @Test
    fun `changed provider inventory rejects approval and acknowledgement is mandatory`() {
        stub(day, listOf("one"))
        val app = sync()
        val preview = app.preview(dates, day)
        assertThrows(InvalidTrainingPeaksPreview::class.java) {
            app.approve(TrainingPeaksBaselineApprovalRequest(dates, preview.revision, false), day)
        }
        stub(day, listOf("two"))
        assertThrows(StaleTrainingPeaksInventory::class.java) {
            app.approve(TrainingPeaksBaselineApprovalRequest(dates, preview.revision, true), day)
        }
        assertNull(db.baselines().latest())
    }

    @Test
    fun `approval preserves pending claims holds ambiguous matches and links only a selected legacy candidate`() {
        val pending = key("pending")
        db.newLedger().claim(pending)
        stub(day, listOf("pending", "double", "single", "unknown"))
        val events = listOf(event(1, "double"), event(2, "double"), event(3, "single"))
        listCalendar(day, events)
        doReturn(events.last()).`when`(destination).getCalendarEvent(target.athleteId, 3)
        val app = sync()
        val preview = app.preview(dates, day)
        val approval = app.approve(
            TrainingPeaksBaselineApprovalRequest(dates, preview.revision, true, mapOf("single" to 3L)), day
        )
        assertEquals(2, approval.heldWorkouts)
        assertEquals(State.PENDING, db.newLedger().find(pending)?.state)
        assertEquals(3L, db.newLedger().find(key("single"))?.remoteEventId)
        assertTrue(db.baselines().isHeld(key("double")))
        assertTrue(db.baselines().isHeld(key("unknown")))
        assertFalse(db.newLedger().claimForSync(key("unknown"), db.baselines().latest()))
    }

    @Test
    fun `preview-only approval and Basic access outside today and tomorrow are blocked`() {
        stub(day, emptyList())
        val inventory = sync().preview(dates, day)
        assertThrows(ResponseStatusException::class.java) {
            sync(previewOnly = true).approve(TrainingPeaksBaselineApprovalRequest(dates, inventory.revision, true), day)
        }
        assertThrows(InvalidTrainingPeaksPreview::class.java) {
            sync().preview(dates.copy(endDate = day.plusDays(2)), day)
        }
        assertNull(db.baselines().latest())
    }

    @Test
    fun `a ledger write on a second connection invalidates the reviewed inventory at the approval boundary`() {
        val repository = db.baselines()
        val accounts = Accounts("source", target.athleteId, repository.generation())
        val reviewedVersion = repository.inventoryVersion()
        assertTrue(db.newLedger().claim(key("concurrent")))

        assertNull(repository.approve(
            accounts, 0, reviewedVersion, "reviewed", day, day, "{}", emptyMap(), emptyMap()
        ))
        assertNull(repository.latest())
        assertEquals(State.PENDING, db.newLedger().find(key("concurrent"))?.state)
    }

    @Test
    fun `an unresolved claim stored under the configured alias blocks a canonical create`() {
        stub(day, listOf("old"))
        val alias = CalendarIdentity(target.athleteId, "Synthetic", "previous-alias")
        doReturn(alias).`when`(destination).resolveCalendarIdentity()
        val oldKey = Key("source", "previous-alias", "old")
        assertTrue(db.newLedger().claim(oldKey))
        val app = sync()
        val preview = app.preview(dates, day)
        assertEquals(TrainingPeaksSyncPreview.Reason.LEGACY_DESTINATION_KEY_UNVERIFIED, preview.workouts.single().reason)
        approve(app)
        sync(enabled = true).copyScheduled(request(day), day)
        assertEquals(State.PENDING, db.newLedger().find(oldKey)?.state)
        assertNull(db.newLedger().find(key("old")))
        assertTrue(db.baselines().isHeld(key("old")))
    }

    @Test
    fun `migration config triggers preserve generation for identical settings and unrelated keys`() {
        val jdbc = db.jdbc()
        jdbc.update("INSERT INTO config(key,value) VALUES ('intervals.api-key','fixture')")
        val generation = db.baselines().generation()
        jdbc.update("UPDATE config SET value='fixture' WHERE key='intervals.api-key'")
        jdbc.update("INSERT INTO config(key,value) VALUES ('review.unrelated','fixture')")
        assertEquals(generation, db.baselines().generation())
        jdbc.update("DELETE FROM config WHERE key='intervals.api-key'")
        assertEquals(generation + 1, db.baselines().generation())
    }

    @Test
    fun `75 historical candidates can be reviewed and only the chosen unmatched hold is created after restart`() {
        stub(day, listOf("new"))
        val events = (1L..75L).map { id ->
            IntervalsCalendarEventDTO(id, "//////////\ntrainingPeaksId=old-$id",
                day.minusDays(id).atStartOfDay(), "WORKOUT", "Ride")
        }
        doReturn(events).`when`(destination).getCalendarSnapshot(target.athleteId, day.minusDays(365), day.plusDays(365))
        events.forEach { doReturn(it).`when`(destination).getCalendarEvent(target.athleteId, it.id) }
        val preview = sync().preview(dates, day)
        val historical = preview.workouts.filter { it.sourceDate == null }
        assertEquals(75, historical.size)
        assertTrue(historical.all { it.reason == TrainingPeaksSyncPreview.Reason.LEGACY_ACCOUNT_UNVERIFIED })
        assertEquals(setOf("new"), preview.workouts.filter { it.sourceDate == day }.mapNotNull { it.sourceWorkoutId }.toSet())
        val links = historical.filterNot { it.sourceWorkoutId == "old-75" }
            .associate { it.sourceWorkoutId!! to it.candidates.single().eventId }
        val approval = sync().approve(TrainingPeaksBaselineApprovalRequest(dates, preview.revision, true, links), day)
        assertEquals(2, approval.heldWorkouts)
        assertEquals(74L, db.newLedger().find(key("old-74"))?.remoteEventId)
        assertTrue(db.baselines().isHeld(key("old-75")))
        assertNull(db.newLedger().find(key("old-75")))
        sync().copy(request(day))
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))

        prepareCreate("new")
        val decision = decision("new")
        assertEquals(501L, sync().resolveUnmatchedHold(decision, day).eventId)
        assertEquals(501L, db.newLedger().find(key("new"))?.remoteEventId)
        assertTrue(db.baselines().isHeld(key("new")))
        assertTrue(db.baselines().isHeld(key("old-75")))
        assertNull(db.newLedger().find(key("old-75")))
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().resolveUnmatchedHold(decision, day) }
        verify(destination, times(1)).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `manual copy overlapping resolution respects pending and confirmed state ahead of retained hold`() {
        stub(day, listOf("new"))
        approve(sync())
        prepareCreate("new")
        val reviewed = decision("new")
        val insidePost = CountDownLatch(1)
        val release = CountDownLatch(1)
        val planned = workout("new", day)
        doAnswer {
            insidePost.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            doReturn(event(501, "new")).`when`(destination).getCalendarEvent(target.athleteId, 501)
            501L
        }.`when`(destination).createScopedCalendarEvent(target, "source", planned)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val resolved = executor.submit<TrainingPeaksHoldResolution> { sync().resolveUnmatchedHold(reviewed, day) }
            assertTrue(insidePost.await(5, TimeUnit.SECONDS))
            assertEquals(State.PENDING, db.newLedger().find(key("new"))?.state)
            assertEquals(WorkoutSyncReview.Reason.UNRESOLVED_WRITE, sync().copy(request(day)).syncReview.single().reason)
            release.countDown()
            assertEquals(501L, resolved.get(5, TimeUnit.SECONDS).eventId)
            assertTrue(db.baselines().isHeld(key("new")))
            assertEquals(TrainingPeaksSyncPreview.Status.VERIFIED_LINK, sync().preview(dates, day).workouts.single().status)
            assertTrue(sync().copy(request(day)).syncReview.isEmpty())
            verify(destination).createScopedCalendarEvent(target, "source", planned)
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun `configuration generation changes invalidate outstanding resolution reviews`() {
        stub(day, listOf("new"))
        approve(sync())
        val reviewed = decision("new")
        db.jdbc().update("INSERT INTO config(key,value) VALUES ('intervals.api-key','synthetic-replacement')")
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().resolveUnmatchedHold(reviewed, day) }
        assertNull(db.newLedger().find(key("new")))
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `hold resolution rejects missing acknowledgement expired visibility modes and changed identity`() {
        stub(day, listOf("new"))
        approve(sync())
        val reviewed = decision("new")
        assertThrows(InvalidTrainingPeaksPreview::class.java) {
            sync().resolveUnmatchedHold(reviewed.copy(acknowledgeManualVerification = false), day)
        }
        assertThrows(ResponseStatusException::class.java) { sync(previewOnly = true).resolveUnmatchedHold(reviewed, day) }
        assertThrows(InvalidTrainingPeaksPreview::class.java) { sync(enabled = true).resolveUnmatchedHold(reviewed, day) }
        assertEquals("BASIC_TODAY_OR_TOMORROW_ONLY", assertThrows(InvalidTrainingPeaksPreview::class.java) {
            sync().resolveUnmatchedHold(reviewed, day.plusDays(1))
        }.reason)
        doReturn(TrainingPeaksUser("different", true, false)).`when`(source).calendarUser()
        doReturn(emptyList<TPWorkoutCalendarResponseDTO>()).`when`(source).getPlannedCalendarEntries("different", day, day)
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().resolveUnmatchedHold(reviewed, day) }
        assertNull(db.newLedger().find(key("new")))
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `editing the source workout invalidates a reviewed hold decision`() {
        stub(day, listOf("new"))
        approve(sync())
        val reviewed = decision("new")
        doReturn(listOf(TPWorkoutCalendarResponseDTO(day.atStartOfDay(), "new", 2, "Edited", 1.0, null, null, null, null)))
            .`when`(source).getPlannedCalendarEntries("source", day, day)
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().resolveUnmatchedHold(reviewed, day) }
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
        assertNull(db.newLedger().find(key("new")))
    }

    @Test
    fun `an event appearing after review and before the final calendar check prevents the claim`() {
        stub(day, listOf("new"))
        approve(sync())
        val reviewed = decision("new")
        val entry = source.getPlannedCalendarEntries("source", day, day).single()
        doAnswer {
            listCalendar(day, listOf(event(35, "new")))
            workout("new", day)
        }.`when`(source).toCalendarWorkout("source", entry)
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().resolveUnmatchedHold(reviewed, day) }
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
        assertNull(db.newLedger().find(key("new")))
    }

    @Test
    fun `concurrent services claim a reviewed hold once even when its POST outcome is uncertain`() {
        stub(day, listOf("new"))
        approve(sync())
        val reviewed = decision("new")
        val entry = source.getPlannedCalendarEntries("source", day, day).single()
        val barrier = CyclicBarrier(2)
        doAnswer {
            barrier.await(5, TimeUnit.SECONDS)
            workout("new", day)
        }.`when`(source).toCalendarWorkout("source", entry)
        doThrow(IllegalStateException("synthetic sensitive upstream failure"))
            .`when`(destination).createScopedCalendarEvent(target, "source", workout("new", day))
        val logger = LoggerFactory.getLogger(TrainingPeaksCalendarSync::class.java) as Logger
        val warnings = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(warnings)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val outcomes = (1..2).map {
                executor.submit<Throwable?> { runCatching { sync().resolveUnmatchedHold(reviewed, day) }.exceptionOrNull() }
            }.map { it.get(10, TimeUnit.SECONDS) }
            assertEquals(1, outcomes.count { it is StaleTrainingPeaksInventory })
            val uncertain = outcomes.filterIsInstance<ResponseStatusException>().single()
            assertEquals("HOLD_WRITE_UNCERTAIN", uncertain.reason)
            assertNull(uncertain.cause)
            val warning = warnings.list.single { "HOLD_WRITE_UNCERTAIN" in it.formattedMessage }
            assertFalse("synthetic sensitive upstream failure" in warning.formattedMessage)
            assertNull(warning.throwableProxy)
        } finally {
            executor.shutdownNow()
            logger.detachAppender(warnings)
            warnings.stop()
        }
        val pending = sync().preview(dates, day).workouts.single()
        assertEquals(TrainingPeaksSyncPreview.Status.UNRESOLVED_CLAIM, pending.status)
        assertFalse(pending.canResolveUnmatchedHold)
        assertEquals(State.PENDING, db.newLedger().find(key("new"))?.state)
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().resolveUnmatchedHold(reviewed, day) }
        verify(destination).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `an ambiguous claim makes a previously held source ineligible even with a fresh preview`() {
        stub(day, listOf("new"))
        approve(sync())
        assertTrue(db.newLedger().recordAmbiguousMatch(key("new")))
        val refreshed = sync().preview(dates, day)
        assertFalse(refreshed.workouts.single().canResolveUnmatchedHold)
        val failure = assertThrows(InvalidTrainingPeaksPreview::class.java) {
            sync().resolveUnmatchedHold(TrainingPeaksHoldResolutionRequest(dates, refreshed.revision, "new", true), day)
        }
        assertEquals("HOLD_NOT_ELIGIBLE", failure.reason)
        assertEquals(State.AMBIGUOUS, db.newLedger().find(key("new"))?.state)
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `repair relinks one account-scoped replacement without writing to the provider`() {
        val replacement = ownedReplacement(502)
        val decision = missingLinkDecision(listOf(replacement))
        assertEquals(TrainingPeaksLinkRepair(502, false), sync().repairMissingLink(decision, day))
        assertEquals(502L, db.newLedger().find(key("new"))?.remoteEventId)
        assertEquals(501L, db.jdbc().queryForObject("SELECT old_event_id FROM trainingpeaks_link_repair", Long::class.java))
        assertEquals("RELINK", db.jdbc().queryForObject("SELECT action FROM trainingpeaks_link_repair", String::class.java))
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().repairMissingLink(decision, day) }
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `automatic and concurrent manual sync cannot restore a missing link or duplicate an explicit repair`() {
        val decision = missingLinkDecision()
        val planned = workout("new", day)
        sync(enabled = true).copyScheduled(request(day), day)
        assertEquals(501L, db.newLedger().find(key("new"))?.remoteEventId)
        verify(destination, never()).createScopedCalendarEvent(target, "source", planned)

        val insidePost = CountDownLatch(1)
        val releasePost = CountDownLatch(1)
        val sourceEntry = source.getPlannedCalendarEntries("source", day, day).single()
        doReturn(planned).`when`(source).toCalendarWorkout("source", sourceEntry)
        doAnswer {
            insidePost.countDown()
            check(releasePost.await(5, TimeUnit.SECONDS))
            doReturn(ownedReplacement(503)).`when`(destination).getCalendarEvent(target.athleteId, 503L)
            503L
        }.`when`(destination).createScopedCalendarEvent(target, "source", planned)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val resolution = executor.submit<TrainingPeaksLinkRepair> { sync().repairMissingLink(decision, day) }
            assertTrue(insidePost.await(5, TimeUnit.SECONDS))
            assertTrue(db.newLedger().hasPendingRepair(key("new")))
            assertEquals(State.PENDING, db.newLedger().find(key("new"))?.state)
            assertEquals(0, sync().copy(request(day)).copied)
            assertThrows(StaleTrainingPeaksInventory::class.java) { sync().repairMissingLink(decision, day) }
            releasePost.countDown()
            assertEquals(TrainingPeaksLinkRepair(503, true), resolution.get(5, TimeUnit.SECONDS))
        } finally {
            releasePost.countDown()
            executor.shutdownNow()
        }
        assertEquals(TrainingPeaksSyncPreview.Status.VERIFIED_LINK, sync().preview(dates, day).workouts.single().status)
        assertEquals(503L, db.jdbc().queryForObject("SELECT new_event_id FROM trainingpeaks_link_repair", Long::class.java))
        assertFalse(db.newLedger().hasPendingRepair(key("new")))
        verify(destination).createScopedCalendarEvent(target, "source", planned)
    }

    @Test
    fun `an uncertain repair stays pending after restart even when a candidate becomes visible`() {
        val decision = missingLinkDecision()
        val sourceEntry = source.getPlannedCalendarEntries("source", day, day).single()
        val planned = workout("new", day)
        doReturn(planned).`when`(source).toCalendarWorkout("source", sourceEntry)
        doThrow(IllegalStateException("synthetic secret")).`when`(destination).createScopedCalendarEvent(target, "source", planned)
        val failure = assertThrows(ResponseStatusException::class.java) { sync().repairMissingLink(decision, day) }
        assertEquals("REPAIR_WRITE_UNCERTAIN", failure.reason)
        assertNull(failure.cause)
        listCalendar(day, listOf(ownedReplacement(503)))
        assertEquals(0, sync().copy(request(day)).copied)
        assertEquals(State.PENDING, db.newLedger().find(key("new"))?.state)
        assertTrue(db.newLedger().hasPendingRepair(key("new")))
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().repairMissingLink(decision, day) }
        verify(destination).createScopedCalendarEvent(target, "source", planned)
    }

    @Test
    fun `repair fails closed on multiple conflicting or malformed destination observations`() {
        val decision = missingLinkDecision()
        for (events in listOf(
            listOf(ownedReplacement(502), ownedReplacement(503)),
            listOf(event(502, "new")),
            listOf(ownedReplacement(502).let {
                IntervalsCalendarEventDTO(it.id, it.description + "\ntrainingPeaksId=ambiguous", it.start_date_local, it.category, it.type)
            }),
        )) {
            listCalendar(day, events)
            val row = sync().preview(dates, day).workouts.single()
            assertNull(row.missingLinkRepair)
            assertEquals("REPAIR_REPLACEMENT_UNVERIFIED", row.repairBlockedReason)
            assertThrows(StaleTrainingPeaksInventory::class.java) { sync().repairMissingLink(decision, day) }
        }
        doThrow(org.freekode.tp2intervals.infrastructure.ProviderReadFailure(
            Platform.INTERVALS, org.freekode.tp2intervals.infrastructure.ProviderReadFailure.Kind.RESPONSE, 206
        )).`when`(destination).getCalendarSnapshot(target.athleteId, day.minusDays(365), day.plusDays(365))
        assertThrows(TrainingPeaksPreviewFailure::class.java) { sync().repairMissingLink(decision, day) }
        assertEquals(501L, db.newLedger().find(key("new"))?.remoteEventId)
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    @Test
    fun `repair rejects stale identity expired Basic visibility and an original event that reappears`() {
        val decision = missingLinkDecision()
        assertThrows(ResponseStatusException::class.java) { sync(previewOnly = true).repairMissingLink(decision, day) }
        assertThrows(InvalidTrainingPeaksPreview::class.java) { sync(enabled = true).repairMissingLink(decision, day) }
        assertThrows(InvalidTrainingPeaksPreview::class.java) { sync().repairMissingLink(decision, day.plusDays(1)) }
        assertThrows(InvalidTrainingPeaksPreview::class.java) {
            sync().repairMissingLink(decision.copy(acknowledgeInconclusive = false), day)
        }
        doReturn(TrainingPeaksUser("other", true, false)).`when`(source).calendarUser()
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().repairMissingLink(decision, day) }
        doReturn(TrainingPeaksUser("source", true, false)).`when`(source).calendarUser()
        doReturn(null, ownedReplacement(501)).`when`(destination).getCalendarEvent(target.athleteId, 501L)
        assertThrows(StaleTrainingPeaksInventory::class.java) { sync().repairMissingLink(decision, day) }
        assertEquals(501L, db.newLedger().find(key("new"))?.remoteEventId)
        verify(destination, never()).createScopedCalendarEvent(target, "source", workout("new", day))
    }

    private fun missingLinkDecision(replacements: List<IntervalsCalendarEventDTO> = emptyList()): TrainingPeaksLinkRepairRequest {
        stub(day, listOf("new"))
        db.seedBaseline("source", target.athleteId)
        assertTrue(db.newLedger().recordLegacyMatch(key("new"), 501))
        listCalendar(day, replacements)
        replacements.forEach { doReturn(it).`when`(destination).getCalendarEvent(target.athleteId, it.id) }
        val preview = sync().preview(dates, day)
        val repair = checkNotNull(preview.workouts.single().missingLinkRepair)
        assertEquals(501L, repair.oldEventId)
        return TrainingPeaksLinkRepairRequest(dates, preview.revision, "new", 501, repair.replacement?.eventId, true, true)
    }

    private fun ownedReplacement(id: Long) = IntervalsCalendarEventDTO(
        id, "//////////\ntrainingPeaksId=new\ntrainingPeaksAccountId=source", day.atStartOfDay(), "WORKOUT", "Ride"
    )

    private fun decision(id: String): TrainingPeaksHoldResolutionRequest {
        val preview = sync().preview(dates, day)
        assertTrue(preview.workouts.single { it.sourceWorkoutId == id }.canResolveUnmatchedHold)
        return TrainingPeaksHoldResolutionRequest(dates, preview.revision, id, true)
    }

    private fun prepareCreate(id: String) {
        val entry = source.getPlannedCalendarEntries("source", day, day).single { it.id == id }
        doReturn(workout(id, day)).`when`(source).toCalendarWorkout("source", entry)
        doReturn(501L).`when`(destination).createScopedCalendarEvent(target, "source", workout(id, day))
    }

    private fun approve(app: TrainingPeaksCalendarSync): TrainingPeaksBaselineApproval {
        val preview = app.preview(dates, day)
        return app.approve(TrainingPeaksBaselineApprovalRequest(dates, preview.revision, true), day)
    }

    private fun sync(enabled: Boolean = false, previewOnly: Boolean = false): TrainingPeaksCalendarSync {
        val mode = PreviewOnly(previewOnly)
        return TrainingPeaksCalendarSync(source, destination, db.newLedger(mode = mode), db.baselines(mode = mode), mapper, mode, enabled)
    }

    private fun stub(date: LocalDate, ids: List<String>) {
        doReturn(TrainingPeaksUser("source", true, false)).`when`(source).calendarUser()
        doReturn(target).`when`(destination).resolveCalendarIdentity()
        doReturn(ids.map { TPWorkoutCalendarResponseDTO(date.atStartOfDay(), it, 2, "Synthetic", 1.0, null, null, null, null) })
            .`when`(source).getPlannedCalendarEntries("source", date, date)
        doReturn(ids.map { workout(it, date) }).`when`(source).getWorkoutsFromCalendar("source", date, date)
        listCalendar(date, emptyList())
    }

    private fun listCalendar(date: LocalDate, events: List<IntervalsCalendarEventDTO>) {
        doReturn(events).`when`(destination).getCalendarSnapshot(target.athleteId, date, date)
        doReturn(events).`when`(destination).getCalendarSnapshot(target.athleteId, date.minusDays(365), date.plusDays(365))
    }

    private fun key(id: String) = Key("source", target.athleteId, id)

    private fun workout(id: String, date: LocalDate) = Workout(
        WorkoutDetails(TrainingType.BIKE, "Synthetic", null, null, null, ExternalData.empty().withTrainingPeaks(id)), date, null
    )

    private fun event(id: Long, sourceId: String) = IntervalsCalendarEventDTO(
        id, "//////////\ntrainingPeaksId=$sourceId", day.atStartOfDay(), "WORKOUT", "Ride"
    )

    private fun request(date: LocalDate) =
        CopyFromCalendarToCalendarRequest(date, date, dates.types, true, Platform.TRAINING_PEAKS, Platform.INTERVALS)
}
