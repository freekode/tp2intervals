package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPToWorkoutConverter
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.TPWorkoutCalendarResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ToIntervalsWorkoutConverterTest {
    @Test
    fun `untimed workout sends a midnight date-time value`() {
        val request = ToIntervalsWorkoutConverter().createEventRequestDTO(workout(null))

        assertEquals("2026-11-23T00:00:00", request.start_date_local)
    }

    @Test
    fun `explicit time including midnight is retained`() {
        val converter = ToIntervalsWorkoutConverter()

        assertEquals("2026-11-23T00:00:00", converter.createEventRequestDTO(workout(LocalTime.MIDNIGHT)).start_date_local)
        assertEquals("2026-11-23T18:00:00", converter.createEventRequestDTO(workout(LocalTime.of(18, 0))).start_date_local)
    }

    @Test
    fun `TrainingPeaks workout with a TrainerRoad marker keeps both IDs in one block`() {
        val date = LocalDate.of(2026, 11, 23)
        val source = TPToWorkoutConverter().toWorkout(TPWorkoutCalendarResponseDTO(
            date.atStartOfDay(), "456", 2, "Workout", null, null,
            "Warmup\r\n//////////\r\ntrainerRoadId=123\r\ntrainingPeaksId=456\r\nintervalsId=789\r\nAdditional notes",
            "Coach notes", null,
        ))
        val converter = ToIntervalsWorkoutConverter()
        val library = LibraryContainer("Library", date, false, 0, ExternalData.empty().withIntervals("1"))

        for (description in listOf(
            converter.createEventRequestDTO(source).description,
            requireNotNull(converter.createWorkoutRequestDTO(library, source).description),
        )) {
            assertEquals(1, "//////////".toRegex().findAll(description).count())
            assertTrue(description.contains("Warmup"))
            assertTrue(description.contains("Additional notes"))
            assertTrue(description.contains("Coach notes"))
            val ids = ExternalData.empty().fromSimpleString(description)
            assertEquals("456", ids.trainingPeaksId)
            assertEquals("123", ids.trainerRoadId)
            assertEquals("789", ids.intervalsId)
            assertTrue(ids.hasClearTrainerRoadId(description))
        }
    }

    @Test
    fun `multiple source marker blocks remain protected when no ID could be parsed`() {
        val source = TPToWorkoutConverter().toWorkout(TPWorkoutCalendarResponseDTO(
            LocalDate.of(2026, 11, 23).atStartOfDay(), "456", 2, "Workout", null, null,
            "//////////\ntrainerRoadId=123\n//////////\ntrainerRoadId=999", null, null,
        ))

        assertEquals(null, source.details.externalData.trainerRoadId)
        assertThrows(PlatformException::class.java) { ToIntervalsWorkoutConverter().createEventRequestDTO(source) }
    }

    @Test
    fun `conflicting TrainingPeaks IDs in an existing marker are rejected`() {
        val source = workout(null).let {
            it.copy(details = it.details.copy(
                description = "//////////\ntrainerRoadId=123\ntrainingPeaksId=456\ntrainingPeaksId=789",
                externalData = ExternalData.empty().withTrainerRoad("123").withTrainingPeaks("456"),
            ))
        }

        assertThrows(PlatformException::class.java) { ToIntervalsWorkoutConverter().createEventRequestDTO(source) }
    }

    @Test
    fun `trainerroad marker must remain unambiguous in calendar and library requests`() {
        val converter = ToIntervalsWorkoutConverter()
        val source = workout(null).let {
            it.copy(details = it.details.copy(
                description = "Warmup\n//////////\ntrainerRoadId=999",
                externalData = ExternalData.empty().withTrainerRoad("123"),
            ))
        }
        val library = LibraryContainer("Library", requireNotNull(source.date), false, 0, ExternalData.empty().withIntervals("1"))

        assertThrows(PlatformException::class.java) { converter.createEventRequestDTO(source) }
        assertThrows(PlatformException::class.java) { converter.createWorkoutRequestDTO(library, source) }

        val valid = source.copy(details = source.details.copy(description = "Warmup"))
        val description = converter.createEventRequestDTO(valid).description
        assertTrue(valid.details.externalData.hasClearTrainerRoadId(description))
        assertTrue(valid.details.externalData.hasClearTrainerRoadId(converter.createWorkoutRequestDTO(library, valid).description))
        val longName = valid.copy(details = valid.details.copy(name = "A".repeat(80) + "//////////"))
        assertThrows(PlatformException::class.java) { converter.createWorkoutRequestDTO(library, longName) }
    }

    private fun workout(time: LocalTime?) = Workout(
        WorkoutDetails(TrainingType.BIKE, "Workout", null, null, null, ExternalData.empty()),
        LocalDate.of(2026, 11, 23),
        null,
        time,
    )
}
