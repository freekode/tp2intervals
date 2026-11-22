package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ToIntervalsWorkoutConverterTest {
    @Test
    fun `untimed workout sends midnight as ISO local date time`() {
        val request = ToIntervalsWorkoutConverter().createEventRequestDTO(workout(null))

        assertEquals("2026-11-23T00:00:00", request.start_date_local)
    }

    @Test
    fun `explicit time including midnight is retained`() {
        val converter = ToIntervalsWorkoutConverter()

        assertEquals("2026-11-23T00:00:00", converter.createEventRequestDTO(workout(LocalTime.MIDNIGHT)).start_date_local)
        assertEquals("2026-11-23T18:00:00", converter.createEventRequestDTO(workout(LocalTime.of(18, 0))).start_date_local)
    }

    private fun workout(time: LocalTime?) = Workout(
        WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null, ExternalData.empty()),
        LocalDate.of(2026, 11, 23),
        null,
        time,
    )
}
