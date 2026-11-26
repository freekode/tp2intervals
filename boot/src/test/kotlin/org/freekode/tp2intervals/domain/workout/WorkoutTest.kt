package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class WorkoutTest {
    @Test
    fun `same trainer road id on the same date is already synced`() {
        val source = workout("Source name", Duration.ofMinutes(60), "123", LocalDate.of(2026, 11, 22))
        val target = workout("Renamed target", Duration.ofMinutes(45), "123", LocalDate.of(2026, 11, 22))

        assertEquals(true, source.isSameForSync(target))
    }

    @Test
    fun `same trainer road id on a different date is not already synced`() {
        val source = workout("Workout", Duration.ofMinutes(60), "123", LocalDate.of(2026, 11, 22))
        val target = workout("Workout", Duration.ofMinutes(60), "123", LocalDate.of(2026, 11, 23))

        assertNotEquals(true, source.isSameForSync(target))
    }

    @Test
    fun `same name and duration on the same date is already synced as fallback`() {
        val source = workout("Workout", Duration.ofMinutes(60), null, LocalDate.of(2026, 11, 22))
        val target = workout("Workout", Duration.ofMinutes(60), null, LocalDate.of(2026, 11, 22))

        assertEquals(true, source.isSameForSync(target))
    }

    @Test
    fun `different trainer road ids are never already synced`() {
        val source = workout("Workout", Duration.ofMinutes(60), "123", LocalDate.of(2026, 11, 22))
        val target = workout("Workout", Duration.ofMinutes(60), "456", LocalDate.of(2026, 11, 22))

        assertEquals(false, source.isSameForSync(target))
    }

    private fun workout(name: String, duration: Duration, trainerRoadId: String?, date: LocalDate): Workout {
        val externalData = trainerRoadId?.let { ExternalData.empty().withTrainerRoad(it) } ?: ExternalData.empty()
        return Workout(
            WorkoutDetails(TrainingType.VIRTUAL_BIKE, name, null, duration, null, externalData),
            date,
            null,
        )
    }
}
