package org.freekode.tp2intervals.app.workout

import config.SyncLedgerFixture
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.config.PreviewOnly
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository.CalendarIdentity
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout.IntervalsEventDTO
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

    private fun listCalendar(date: LocalDate, events: List<IntervalsEventDTO>) {
        doReturn(events).`when`(destination).getCalendarSnapshot(target.athleteId, date, date)
        doReturn(events).`when`(destination).getCalendarSnapshot(target.athleteId, date.minusDays(365), date.plusDays(365))
    }

    private fun key(id: String) = Key("source", target.athleteId, id)

    private fun workout(id: String, date: LocalDate) = Workout(
        WorkoutDetails(TrainingType.BIKE, "Synthetic", null, null, null, ExternalData.empty().withTrainingPeaks(id)), date, null
    )

    private fun event(id: Long, sourceId: String) = IntervalsEventDTO(
        id, "Synthetic", "//////////\ntrainingPeaksId=$sourceId", day.atStartOfDay(), "WORKOUT", "Ride", null, null, null
    )

    private fun request(date: LocalDate) =
        CopyFromCalendarToCalendarRequest(date, date, dates.types, true, Platform.TRAINING_PEAKS, Platform.INTERVALS)
}
