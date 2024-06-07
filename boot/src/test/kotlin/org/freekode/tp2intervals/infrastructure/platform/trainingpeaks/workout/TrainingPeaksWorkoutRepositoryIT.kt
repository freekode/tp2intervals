package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import config.SpringITConfig
import java.time.LocalDate
import java.time.LocalDateTime
import org.freekode.tp2intervals.app.plan.CopyLibraryRequest
import org.freekode.tp2intervals.app.plan.DeleteLibraryRequest
import org.freekode.tp2intervals.app.plan.LibraryService
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.workout.CopyFromCalendarToLibraryRequest
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.StepModifier
import org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TrainingPeaksWorkoutRepositoryIT : SpringITConfig() {
    @Autowired
    lateinit var libraryService: LibraryService

    @Autowired
    lateinit var workoutService: WorkoutService

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            Thread.sleep(1000)
        }
    }

    @Test
    fun `should sync planned workouts`() {
        val startDate = LocalDate.parse("2024-03-11")
        val endDate = LocalDate.parse("2024-03-17")
        val deleteRequest = DeleteWorkoutRequestDTO(startDate, endDate, Platform.TRAINING_PEAKS)
        workoutService.deleteWorkoutsFromCalendar(deleteRequest)

        val copyRequest = CopyFromCalendarToCalendarRequest(
            startDate, endDate,
            TrainingType.DEFAULT_LIST,
            true,
            Platform.INTERVALS,
            Platform.TRAINING_PEAKS
        )
        val response = workoutService.copyWorkoutsFromCalendarToCalendar(copyRequest)

        workoutService.deleteWorkoutsFromCalendar(deleteRequest)


        Assertions.assertEquals(response.copied, 5)
    }

    @Test
    fun `should copy plan`() {
        val plan = libraryService.getLibraryContainers(Platform.TRAINING_PEAKS)
            .find { it.name == "Welcome Plan for Cyclists" }
        val response = libraryService.copyLibrary(
            CopyLibraryRequest(
                plan!!,
                "${plan.name} ${LocalDateTime.now()}",
                StepModifier.NONE,
                Platform.TRAINING_PEAKS,
                Platform.INTERVALS
            )
        )
        libraryService.deleteLibrary(DeleteLibraryRequest(response.externalData, Platform.INTERVALS))

        Assertions.assertEquals(response.workouts, 23)
    }

    @Test
    fun `should copy planned workouts to library`() {
        val response = workoutService.copyWorkoutsFromCalendarToLibrary(
            CopyFromCalendarToLibraryRequest(
                LocalDate.parse("2024-03-04"),
                LocalDate.parse("2024-03-10"),
                "My Test Library ${LocalDateTime.now()}",
                true,
                TrainingType.DEFAULT_LIST,
                Platform.TRAINING_PEAKS,
                Platform.INTERVALS
            )
        )
        libraryService.deleteLibrary(DeleteLibraryRequest(response.externalData, Platform.INTERVALS))

        Assertions.assertEquals(response.copied, 5)
    }
}
