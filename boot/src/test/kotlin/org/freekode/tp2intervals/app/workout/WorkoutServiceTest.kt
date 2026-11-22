package org.freekode.tp2intervals.app.workout

import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.app.workout.schedule.C2CTodayScheduledRequest
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate

class WorkoutServiceTest {
    private val date = LocalDate.of(2026, 11, 23)
    private val source = mock(WorkoutRepository::class.java)
    private val target = mock(WorkoutRepository::class.java)

    private fun service(): WorkoutService {
        `when`(source.platform()).thenReturn(Platform.TRAINER_ROAD)
        `when`(target.platform()).thenReturn(Platform.INTERVALS)
        return WorkoutService(listOf(source, target), emptyList())
    }

    @Test
    fun `keeps matching IDs, copies missing workouts and deletes stale TrainerRoad events`() {
        val matched = workout("1")
        val new = workout("2")
        val filtered = workout("3", TrainingType.RUN)
        val matchedTarget = target(matched, "10", "Old name")
        val filteredTarget = target(filtered, "30")
        val stale = target(workout("4"), "40")
        val unrelated = workout("5").copy(details = workout("5").details.copy(externalData = ExternalData.empty().withIntervals("50")))
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matched, new, filtered))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(matchedTarget, filteredTarget, stale, unrelated))

        val result = service().copyWorkoutsC2C(request())

        assertEquals(1, result.copied)
        assertEquals(1, result.alreadySynced)
        assertEquals(1, result.removed)
        assertEquals(1, result.filteredOut)
        val order = inOrder(target)
        order.verify(target).saveWorkoutsToCalendar(listOf(new))
        order.verify(target).deleteSyncedWorkout(stale)
        verify(target, never()).deleteSyncedWorkout(matchedTarget)
        verify(target, never()).deleteSyncedWorkout(filteredTarget)
        verify(target, never()).deleteSyncedWorkout(unrelated)
    }

    @Test
    fun `empty source removes identifiable workouts but not unrelated events`() {
        val stale = target(workout("1", TrainingType.RUN), "10")
        val unrelated = workout("2").copy(details = workout("2").details.copy(externalData = ExternalData.empty().withIntervals("20")))
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(emptyList())
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(stale, unrelated))

        val result = service().copyWorkoutsC2C(request())

        assertEquals(1, result.removed)
        verify(target).deleteSyncedWorkout(stale)
        verify(target, never()).deleteSyncedWorkout(unrelated)
    }

    @Test
    fun `same ID on a different day is copied and old event removed`() {
        val moved = workout("1")
        val old = target(moved.copy(date = date.plusDays(1)), "10")
        `when`(source.getWorkoutsForReconciliation(date, date.plusDays(1))).thenReturn(listOf(moved))
        `when`(target.getWorkoutsForReconciliation(date, date.plusDays(1))).thenReturn(listOf(old))

        val result = service().copyWorkoutsC2C(request().copy(endDate = date.plusDays(1)))

        assertEquals(1, result.copied)
        assertEquals(1, result.removed)
        verify(target).saveWorkoutsToCalendar(listOf(moved))
        verify(target).deleteSyncedWorkout(old)
    }

    @Test
    fun `ambiguous IDs are skipped even when no match exists`() {
        val duplicatedSource = workout("1")
        val sourceWithDuplicateTargets = workout("2")
        val target1 = target(sourceWithDuplicateTargets, "20")
        val target2 = target(sourceWithDuplicateTargets, "21")
        val stale1 = target(workout("3"), "30")
        val stale2 = target(workout("3"), "31")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(duplicatedSource, duplicatedSource, sourceWithDuplicateTargets))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(target1, target2, stale1, stale2))

        val result = service().copyWorkoutsC2C(request())

        assertEquals(5, result.skipped)
        assertEquals(0, result.copied)
        assertEquals(0, result.removed)
        verify(target).saveWorkoutsToCalendar(emptyList())
        verify(target, never()).deleteSyncedWorkout(stale1)
        verify(target, never()).deleteSyncedWorkout(stale2)
    }

    @Test
    fun `copy failure prevents deletions`() {
        val new = workout("1")
        val stale = target(workout("2"), "20")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(new))
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(stale))
        doThrow(IllegalStateException("copy failed")).`when`(target).saveWorkoutsToCalendar(listOf(new))

        assertThrows(IllegalStateException::class.java) { service().copyWorkoutsC2C(request()) }
        verify(target, never()).deleteSyncedWorkout(stale)
    }

    @Test
    fun `delete failure propagates rather than reporting success`() {
        val stale = target(workout("1"), "10")
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(emptyList())
        `when`(target.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(stale))
        doThrow(IllegalStateException("delete failed")).`when`(target).deleteSyncedWorkout(stale)

        assertThrows(IllegalStateException::class.java) { service().copyWorkoutsC2C(request()) }
    }

    @Test
    fun `incomplete source identity prevents deletions`() {
        val invalid = workout("1").copy(details = workout("1").details.copy(externalData = ExternalData.empty()))
        `when`(source.getWorkoutsForReconciliation(date, date)).thenReturn(listOf(invalid))

        assertThrows(PlatformException::class.java) { service().copyWorkoutsC2C(request()) }
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
    }

    private fun request() = CopyFromCalendarToCalendarRequest(
        date, date, listOf(TrainingType.BIKE), true, Platform.TRAINER_ROAD, Platform.INTERVALS,
        removeStaleTrainerRoad = true,
    )

    private fun workout(id: String, type: TrainingType = TrainingType.BIKE) = Workout(
        WorkoutDetails(type, "Workout $id", null, null, null, ExternalData.empty().withTrainerRoad(id)),
        date,
        null,
    )

    private fun target(workout: Workout, eventId: String, name: String = workout.details.name) = workout.copy(
        details = workout.details.copy(name = name, externalData = workout.details.externalData.withIntervals(eventId)),
    )
}
