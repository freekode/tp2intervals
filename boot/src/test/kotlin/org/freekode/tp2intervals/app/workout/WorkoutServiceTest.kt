package org.freekode.tp2intervals.app.workout

import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.domain.workout.TrainerRoadReconciliationSource
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class WorkoutServiceTest {
    private val date = LocalDate.of(2026, 11, 23)
    private val source = mock(WorkoutRepository::class.java)
    private val target = mock(WorkoutRepository::class.java)

    private fun service(): WorkoutService {
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(target.platform()).thenReturn(Platform.INTERVALS)
        org.mockito.Mockito.doAnswer {
            TrainerRoadReconciliationSource(source.getWorkoutsForReconciliation(date, date), emptyList())
        }.`when`(source).getTrainerRoadReconciliationSource(date, date)
        return WorkoutService(listOf(source, target), emptyList())
    }

    @Test
    fun `legacy removal is rejected before calendar access`() {
        assertThrows(PlatformException::class.java) { service().copyWorkoutsC2C(request().copy(removeStaleTrainerRoad = true)) }
        verify(source, never()).getWorkoutsForReconciliation(date, date)
        verify(target, never()).getWorkoutsForReconciliation(date, date)
    }

    @Test
    fun `old refresh requests cannot silently enable deletions`() {
        assertThrows(PlatformException::class.java) {
            service().copyWorkoutsC2C(request().copy(refreshSynced = true, removeStaleTrainerRoad = false))
        }
        verify(source, never()).getWorkoutsForReconciliation(date, date)
        verify(target, never()).getWorkoutsForReconciliation(date, date)
    }

    @Test
    fun `scheduled reconciliation option is passed to the daily request`() {
        val scheduled = C2CTodayScheduledRequest(
            listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS, "18:00", removeStaleTrainerRoad = true,
        )
        assertEquals(true, scheduled.forToday().removeStaleTrainerRoad)
        val legacy = ObjectMapperFactory.objectMapper().readValue(
            """{"types":["BIKE"],"skipSynced":true,"sourcePlatform":"TRAINER_ROAD","targetPlatform":"INTERVALS"}""",
            C2CTodayScheduledRequest::class.java,
        )
        assertEquals(false, legacy.forToday().removeStaleTrainerRoad)
        assertEquals(false, legacy.mirrorByTrainerRoadId)
    }

    @Test
    fun `preview is read only and add missing mode never updates or deletes`() {
        val matched = workout("1")
        val new = workout("2")
        val existing = target(matched, "10")
        val stale = target(workout("3"), "30")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched, new))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing, stale))
        val request = request().copy(removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)
        val service = service()

        val preview = service.previewTrainerRoadSync(request)
        assertEquals(1, preview.copied)
        assertEquals(1, preview.alreadySynced)
        assertEquals(0, preview.updated)
        assertEquals(0, preview.removed)
        verify(target, never()).saveTrainerRoadWorkoutsToCalendar(listOf(new))

        val result = service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        assertEquals(preview, result)
        verify(target).saveTrainerRoadWorkoutsToCalendar(listOf(new))
        verify(target, never()).updateSyncedWorkout(matched, existing)
        verify(target, never()).deleteSyncedWorkout(stale)
    }

    @Test
    fun `mirror requires consent and updates matches before deleting stale events`() {
        val matched = workout("1")
        val existing = target(matched, "10")
        val stale = target(workout("2"), "20")
        val request = request().copy(removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR)
        val service = service()
        assertThrows(PlatformException::class.java) { service.previewTrainerRoadSync(request) }
        assertThrows(PlatformException::class.java) { service.copyWorkoutsC2C(request) }
        verify(source, never()).getWorkoutsForReconciliation(date, date)

        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing, stale))
        val result = service.copyWorkoutsC2C(request.copy(allowStaleRemoval = true))
        assertEquals(1, result.updated)
        assertEquals(1, result.removed)
        val order = inOrder(target)
        order.verify(target).saveTrainerRoadWorkoutsToCalendar(emptyList())
        order.verify(target).updateSyncedWorkout(matched, existing)
        order.verify(target).deleteSyncedWorkout(stale)
    }

    @Test
    fun `mirror leaves unchanged workouts alone and counts them as already synced`() {
        val matched = workout("1")
        val existing = target(matched, "10")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing))
        `when`(target.isTrainerRoadWorkoutUpToDate(matched, existing)).thenReturn(true)
        val request = request().copy(trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)
        val service = service()

        val preview = service.previewTrainerRoadSync(request)
        assertEquals(1, preview.alreadySynced)
        assertEquals(0, preview.updated)
        assertTrue(preview.previewActions.isEmpty())
        val result = service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        assertEquals(preview, result)
        verify(target, never()).updateSyncedWorkout(matched, existing)
        verify(target, never()).deleteSyncedWorkout(existing)
    }

    @Test
    fun `mirror aborts if an unchanged workout changes after preview`() {
        val matched = workout("1")
        val existing = target(matched, "10")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing))
        `when`(target.isTrainerRoadWorkoutUpToDate(matched, existing)).thenReturn(true, false)
        val request = request().copy(trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)
        val service = service()
        val preview = service.previewTrainerRoadSync(request)

        assertThrows(PlatformException::class.java) {
            service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        }
        verify(target, never()).updateSyncedWorkout(matched, existing)
    }

    @Test
    fun `mirror copies a moved workout and removes its old event only after the copy succeeds`() {
        val moved = workout("1")
        val old = target(moved.copy(date = date.plusDays(1)), "10")
        val service = service()
        `when`(source.getTrainerRoadReconciliationSource(date, date.plusDays(1)))
            .thenReturn(TrainerRoadReconciliationSource(listOf(moved), emptyList()))
        `when`(target.getWorkoutsForReconciliation(date, date.plusDays(1))).thenReturn(listOf(old))
        val request = request().copy(endDate = date.plusDays(1), trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)
        doThrow(IllegalStateException("copy failed")).`when`(target).saveTrainerRoadWorkoutsToCalendar(listOf(moved))

        assertThrows(IllegalStateException::class.java) { service.copyWorkoutsC2C(request) }
        verify(target, never()).deleteSyncedWorkout(old)
    }

    @Test
    fun `new mode skips ambiguous source and target IDs`() {
        val duplicated = workout("1")
        val matched = workout("2")
        val first = target(matched, "20")
        val second = target(matched, "21")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(duplicated, duplicated, matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(first, second))
        val request = request().copy(removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        val result = service().copyWorkoutsC2C(request)

        assertEquals(3, result.skipped)
        assertEquals(0, result.copied)
        assertEquals(0, result.updated)
        assertEquals(
            listOf(SkippedSyncReason.DUPLICATE_TRAINERROAD_ID, SkippedSyncReason.DUPLICATE_TRAINERROAD_ID, SkippedSyncReason.DUPLICATE_INTERVALS_ID),
            result.skippedWorkouts.map { it.reason },
        )
        assertEquals(listOf("20", "21"), result.skippedWorkouts.last().intervalsIds)
    }

    @Test
    fun `unidentified activities are reported but do not prevent removal of marked stale workouts`() {
        val nextDay = date.plusDays(1)
        val new = workout("1")
        val staleOnSameDay = target(workout("2"), "20")
        val staleOnOtherDay = target(workout("3").copy(date = nextDay), "30")
        val protected = target(workout("4"), "40", clearId = false)
        val service = service()
        `when`(source.getTrainerRoadReconciliationSource(date, nextDay))
            .thenReturn(TrainerRoadReconciliationSource(listOf(new), listOf(date)))
        `when`(target.getWorkoutsForReconciliation(date, nextDay))
            .thenReturn(listOf(staleOnSameDay, staleOnOtherDay, protected))
        val request = request().copy(
            endDate = nextDay, removeStaleTrainerRoad = false,
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )

        val preview = service.previewTrainerRoadSync(request)
        assertEquals(listOf(date), preview.unidentifiedActivityDates)
        assertEquals(1, preview.copied)
        assertEquals(2, preview.removed)
        assertEquals(1, preview.skipped)
        assertEquals(SkippedSyncReason.PROTECTED_EVENT, preview.skippedWorkouts.single().reason)

        val result = service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        assertEquals(preview, result)
        verify(target).saveTrainerRoadWorkoutsToCalendar(listOf(new))
        verify(target).deleteSyncedWorkout(staleOnSameDay)
        verify(target).deleteSyncedWorkout(staleOnOtherDay)
        verify(target, never()).deleteSyncedWorkout(protected)
    }

    @Test
    fun `mirror aborts when an unidentified activity appears after preview`() {
        val stale = target(workout("2"), "20")
        val service = service()
        org.mockito.Mockito.doReturn(
            TrainerRoadReconciliationSource(emptyList(), emptyList()),
            TrainerRoadReconciliationSource(emptyList(), listOf(date)),
        ).`when`(source).getTrainerRoadReconciliationSource(date, date)
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(stale))
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )
        val preview = service.previewTrainerRoadSync(request)

        assertEquals(1, preview.removed)
        assertThrows(PlatformException::class.java) {
            service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        }
        verify(target, never()).deleteSyncedWorkout(stale)
        verify(target, never()).saveTrainerRoadWorkoutsToCalendar(emptyList())
    }

    @Test
    fun `mirror rejects a preview confirmation without unidentified activity dates`() {
        val stale = target(workout("2"), "20")
        val service = service()
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(emptyList())
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(stale))
        val request = request().copy(trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)
        val preview = service.previewTrainerRoadSync(request)

        assertThrows(PlatformException::class.java) {
            service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions))
        }
        verify(target, never()).deleteSyncedWorkout(stale)
    }

    @Test
    fun `mirror rejects invalid source IDs before reading or changing the target`() {
        val service = service()
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(workout("0")))
        val request = request().copy(trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true)

        assertThrows(PlatformException::class.java) { service.copyWorkoutsC2C(request) }
        verify(target, never()).getWorkoutsForReconciliation(date, date)
        verify(target, never()).saveTrainerRoadWorkoutsToCalendar(emptyList())
    }

    @Test
    fun `mirror plans a moved workout and removal of its old marked event despite unidentified activities`() {
        val moved = workout("1")
        val old = target(moved.copy(date = date.plusDays(1)), "10")
        val service = service()
        `when`(source.getTrainerRoadReconciliationSource(date, date.plusDays(2)))
            .thenReturn(TrainerRoadReconciliationSource(listOf(moved), listOf(date.plusDays(2))))
        `when`(target.getWorkoutsForReconciliation(date, date.plusDays(2))).thenReturn(listOf(old))
        val request = request().copy(
            endDate = date.plusDays(2), trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )

        val preview = service.previewTrainerRoadSync(request)

        assertEquals(1, preview.copied)
        assertEquals(1, preview.removed)
        assertEquals(listOf(date.plusDays(2)), preview.unidentifiedActivityDates)
        service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        verify(target).saveTrainerRoadWorkoutsToCalendar(listOf(moved))
        verify(target).deleteSyncedWorkout(old)
    }

    @Test
    fun `add missing reports unidentified activities without blocking identifiable workouts`() {
        val new = workout("1")
        val service = service()
        org.mockito.Mockito.doReturn(TrainerRoadReconciliationSource(listOf(new), listOf(date)))
            .`when`(source).getTrainerRoadReconciliationSource(date, date)
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(emptyList())
        val request = request().copy(removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)

        val preview = service.previewTrainerRoadSync(request)
        assertEquals(1, preview.copied)
        assertEquals(listOf(date), preview.unidentifiedActivityDates)
        val result = service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        assertEquals(preview, result)
        verify(target).saveTrainerRoadWorkoutsToCalendar(listOf(new))
    }

    @Test
    fun `unidentified activity outside requested range does not pause stale removal`() {
        val stale = target(workout("2"), "20")
        val service = service()
        org.mockito.Mockito.doReturn(TrainerRoadReconciliationSource(emptyList(), listOf(date.plusDays(1))))
            .`when`(source).getTrainerRoadReconciliationSource(date, date)
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(stale))
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )

        val preview = service.previewTrainerRoadSync(request)
        assertEquals(1, preview.removed)
        assertTrue(preview.unidentifiedActivityDates.isEmpty())
        assertEquals(0, preview.skipped)
        service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        verify(target).deleteSyncedWorkout(stale)
    }

    @Test
    fun `failed update prevents stale deletions`() {
        val matched = workout("1")
        val existing = target(matched, "10")
        val stale = target(workout("2"), "20")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing, stale))
        doThrow(IllegalStateException("update failed")).`when`(target).updateSyncedWorkout(matched, existing)
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )

        assertThrows(IllegalStateException::class.java) { service().copyWorkoutsC2C(request) }
        verify(target, never()).deleteSyncedWorkout(stale)
    }

    @Test
    fun `scheduled mode runs only for today with consent preserved`() {
        val scheduled = C2CTodayScheduledRequest(
            listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true, mirrorByTrainerRoadId = true,
        )
        val request = scheduled.forToday()
        assertEquals(LocalDate.now(), request.startDate)
        assertEquals(request.startDate, request.endDate)
        assertEquals(TrainerRoadSyncMode.MIRROR, request.trainerRoadSyncMode)
        assertEquals(true, request.allowStaleRemoval)
        assertEquals(true, scheduled.mirrorByTrainerRoadId)
    }

    @Test
    fun `changed plan after preview aborts before any writes`() {
        val matched = workout("1")
        val existing = target(matched, "10")
        val stale = target(workout("2"), "20")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date))
            .thenReturn(listOf(existing, stale), listOf(existing))
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )
        val service = service()
        val preview = service.previewTrainerRoadSync(request)
        assertEquals(listOf(SyncAction.UPDATE, SyncAction.REMOVE), preview.previewActions.map { it.action })

        assertThrows(PlatformException::class.java) {
            service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates))
        }
        verify(target, never()).saveTrainerRoadWorkoutsToCalendar(emptyList())
        verify(target, never()).updateSyncedWorkout(matched, existing)
        verify(target, never()).deleteSyncedWorkout(stale)
    }

    @Test
    fun `preview actions without an Intervals ID deserialize for confirmation`() {
        val request = ObjectMapperFactory.objectMapper().readValue(
            """{"startDate":"2026-11-23","endDate":"2026-11-23","types":["BIKE"],"skipSynced":true,"sourcePlatform":"TRAINER_ROAD","targetPlatform":"INTERVALS","trainerRoadSyncMode":"ADD_MISSING","expectedActions":[{"action":"ADD","date":"2026-11-23","name":"Workout","trainerRoadId":"123"}]}""",
            CopyFromCalendarToCalendarRequest::class.java,
        )
        val action = requireNotNull(request.expectedActions).single()
        assertEquals(SyncAction.ADD, action.action)
        assertEquals(null, action.intervalsId)
    }

    @Test
    fun `new modes leave events without TrainerRoad identity untouched`() {
        val sourceWorkout = workout("1")
        val unrelated = sourceWorkout.copy(details = sourceWorkout.details.copy(externalData = ExternalData.empty().withIntervals("10")))
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(sourceWorkout))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(unrelated))
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )

        val result = service().copyWorkoutsC2C(request)

        assertEquals(1, result.copied)
        assertEquals(0, result.updated)
        assertEquals(0, result.removed)
        verify(target, never()).updateSyncedWorkout(sourceWorkout, unrelated)
        verify(target, never()).deleteSyncedWorkout(unrelated)
    }

    @Test
    fun `mirror protects events without a clear TrainerRoad ID`() {
        val matched = workout("1")
        val untrackedMatch = target(matched, "10", clearId = false).copy(
            details = matched.details.copy(
                description = org.freekode.tp2intervals.infrastructure.Signature.description,
                externalData = matched.details.externalData.withIntervals("10"),
            ),
        )
        val untrackedStale = target(workout("2"), "20", clearId = false)
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(untrackedMatch, untrackedStale))
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true,
        )

        val result = service().copyWorkoutsC2C(request)

        assertEquals(0, result.copied)
        assertEquals(0, result.updated)
        assertEquals(0, result.removed)
        assertEquals(2, result.skipped)
        assertTrue(result.skippedWorkouts.all { it.reason == SkippedSyncReason.PROTECTED_EVENT })
        verify(target, never()).updateSyncedWorkout(matched, untrackedMatch)
        verify(target, never()).deleteSyncedWorkout(untrackedStale)
    }

    @Test
    fun `mirror updates and removes events with clear IDs but skips ambiguous matches`() {
        val matched = workout("1")
        val duplicate = workout("2")
        val existing = target(matched, "10")
        val first = target(duplicate, "20")
        val second = target(duplicate, "21")
        val stale = target(workout("3"), "30")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched, duplicate))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing, first, second, stale))
        val request = request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR,
            allowStaleRemoval = true,
        )
        val service = service()
        val preview = service.previewTrainerRoadSync(request)
        assertEquals(1, preview.updated)
        assertEquals(1, preview.removed)
        assertEquals(1, preview.skipped)

        assertEquals(preview, service.copyWorkoutsC2C(request.copy(expectedActions = preview.previewActions, expectedUnidentifiedActivityDates = preview.unidentifiedActivityDates)))
        verify(target).updateSyncedWorkout(matched, existing)
        verify(target).deleteSyncedWorkout(stale)
        verify(target, never()).updateSyncedWorkout(duplicate, first)
        verify(target, never()).deleteSyncedWorkout(second)
    }

    @Test
    fun `add missing skips an untracked matching event without duplicating it`() {
        val matched = workout("1")
        val untracked = target(matched, "10")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(untracked))
        val result = service().copyWorkoutsC2C(request().copy(
            removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING,
        ))

        assertEquals(0, result.copied)
        assertEquals(1, result.alreadySynced)
        verify(target, never()).updateSyncedWorkout(matched, untracked)
        verify(target, never()).deleteSyncedWorkout(untracked)
    }

    @Test
    fun `overlapping syncs recheck the calendar after the first finishes`() {
        val sourceWorkout = workout("1")
        val existing = AtomicReference<Workout?>(null)
        val firstSave = CountDownLatch(1)
        val releaseSave = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)
        val secondRead = CountDownLatch(1)
        val reads = AtomicInteger()
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(sourceWorkout))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenAnswer {
            if (reads.incrementAndGet() == 2) secondRead.countDown()
            listOfNotNull(existing.get())
        }
        org.mockito.Mockito.doAnswer { invocation ->
            val toSave = invocation.getArgument<List<Workout>>(0)
            if (toSave.isNotEmpty()) {
                firstSave.countDown()
                check(releaseSave.await(5, TimeUnit.SECONDS))
                existing.set(sourceWorkout.copy(details = sourceWorkout.details.copy(
                    externalData = sourceWorkout.details.externalData.withIntervals("10"),
                )))
            }
            null
        }.`when`(target).saveTrainerRoadWorkoutsToCalendar(org.mockito.ArgumentMatchers.anyList())
        val service = service()
        val request = request().copy(removeStaleTrainerRoad = false, trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val first = executor.submit<CopyWorkoutsResponse> { service.copyWorkoutsC2C(request) }
            assertTrue(firstSave.await(5, TimeUnit.SECONDS))
            val second = executor.submit<CopyWorkoutsResponse> {
                secondStarted.countDown()
                service.copyWorkoutsC2C(request)
            }
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS))
            assertFalse(secondRead.await(300, TimeUnit.MILLISECONDS))
            releaseSave.countDown()

            assertEquals(1, first.get(5, TimeUnit.SECONDS).copied)
            val result = second.get(5, TimeUnit.SECONDS)
            assertEquals(0, result.copied)
            assertEquals(1, result.alreadySynced)
        } finally {
            releaseSave.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun `legacy skip synced uses event identity`() {
        val sourceWorkout = workout("1")
        val tracked = target(sourceWorkout, "10", "Renamed")
        `when`(source.getWorkoutsFromCalendar(date, date)).thenReturn(listOf(sourceWorkout))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(tracked))

        val result = service().copyWorkoutsC2C(request().copy(removeStaleTrainerRoad = false))

        assertEquals(0, result.copied)
        assertEquals(1, result.alreadySynced)
    }

    @Test
    fun `TrainerRoad time takes priority over fallback for new workouts in both sync paths`() {
        val timed = workout("1").withStartTime(LocalTime.of(7, 30))
        val untimed = workout("2")
        val savedTimes = mutableListOf<List<LocalTime?>>()
        `when`(source.getWorkoutsFromCalendar(date, date)).thenReturn(listOf(timed, untimed))
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(timed, untimed))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(emptyList())
        org.mockito.Mockito.doAnswer { invocation ->
            savedTimes.add(invocation.getArgument<List<Workout>>(0).map { it.startTime })
            null
        }.`when`(target).saveTrainerRoadWorkoutsToCalendar(org.mockito.ArgumentMatchers.anyList())
        val service = service()

        service.copyWorkoutsC2C(request().copy(skipSynced = false, defaultStartTime = "18:00"))
        service.copyWorkoutsC2C(request().copy(trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING, defaultStartTime = "18:00"))
        service.copyWorkoutsC2C(request().copy(trainerRoadSyncMode = TrainerRoadSyncMode.ADD_MISSING))

        assertEquals(listOf(
            listOf(LocalTime.of(7, 30), LocalTime.of(18, 0)),
            listOf(LocalTime.of(7, 30), LocalTime.of(18, 0)),
            listOf(LocalTime.of(7, 30), null),
        ), savedTimes)
    }

    @Test
    fun `mirror compares and updates with TrainerRoad time instead of default`() {
        val timed = workout("1").withStartTime(LocalTime.of(7, 30))
        val existing = target(timed, "10")
        val compared = mutableListOf<LocalTime?>()
        val updated = mutableListOf<LocalTime?>()
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(timed))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(existing))
        org.mockito.Mockito.doAnswer { invocation ->
            compared.add(invocation.getArgument<Workout>(0).startTime)
            false
        }.`when`(target).isTrainerRoadWorkoutUpToDate(timed, existing)
        org.mockito.Mockito.doAnswer { invocation ->
            updated.add(invocation.getArgument<Workout>(0).startTime)
            null
        }.`when`(target).updateSyncedWorkout(timed, existing)

        service().copyWorkoutsC2C(request().copy(
            trainerRoadSyncMode = TrainerRoadSyncMode.MIRROR, allowStaleRemoval = true, defaultStartTime = "18:00",
        ))

        assertEquals(listOf(LocalTime.of(7, 30)), compared)
        assertEquals(compared, updated)
    }

    private fun request() = CopyFromCalendarToCalendarRequest(
        date, date, listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
        removeStaleTrainerRoad = false,
    )

    private fun workout(id: String, type: TrainingType = TrainingType.BIKE) = Workout(
        WorkoutDetails(type, "Workout $id", null, null, null, ExternalData.empty().withTrainerRoad(id)),
        date,
        null,
    )

    private fun target(workout: Workout, eventId: String, name: String = workout.details.name, clearId: Boolean = true): Workout {
        val result = workout.copy(details = workout.details.copy(name = name, externalData = workout.details.externalData.withIntervals(eventId)))
        `when`(target.hasExplicitTrainerRoadId(result)).thenReturn(clearId)
        return result
    }
}
