package org.freekode.tp2intervals.app.workout

import config.BaseSpringITConfig
import org.freekode.tp2intervals.app.plan.CopyLibraryRequest
import org.freekode.tp2intervals.app.plan.DeleteLibraryRequest
import org.freekode.tp2intervals.app.plan.LibraryService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.StepModifier
import org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime

class TrainingPeaksWorkoutServiceIT : BaseSpringITConfig() {
    @Autowired
    lateinit var libraryService: LibraryService

    @Autowired
    lateinit var workoutService: WorkoutService

    private val platform = Platform.TRAINING_PEAKS

    @Test
    fun `should sync planned workouts`() {
        val startDate = LocalDate.parse("2024-03-11")
        val endDate = LocalDate.parse("2024-03-17")
        val deleteRequest = DeleteWorkoutRequestDTO(startDate, endDate, platform)
        workoutService.deleteWorkoutsFromCalendar(deleteRequest)

        val copyRequest = CopyFromCalendarToCalendarRequest(
            startDate, endDate,
            TrainingType.DEFAULT_LIST,
            true,
            Platform.INTERVALS,
            platform
        )
        val response = workoutService.copyWorkoutsC2C(copyRequest)

        workoutService.deleteWorkoutsFromCalendar(deleteRequest)


        Assertions.assertEquals(response.copied, 5)
    }

    @Test
    fun `should copy plan`() {
        val plan = libraryService.findByPlatform(platform)
            .find { it.name == "Welcome Plan for Cyclists" }
        val response = libraryService.copyLibrary(
            CopyLibraryRequest(
                plan!!,
                "${plan.name} ${LocalDateTime.now()}",
                StepModifier.NONE,
                platform,
                Platform.INTERVALS
            )
        )
        libraryService.deleteLibrary(DeleteLibraryRequest(response.externalData, Platform.INTERVALS))

        Assertions.assertEquals(response.workouts, 23)
    }

    @Test
    fun `should copy planned workouts to library`() {
        val response = workoutService.copyWorkoutsC2L(
            CopyFromCalendarToLibraryRequest(
                LocalDate.parse("2024-03-04"),
                LocalDate.parse("2024-03-10"),
                "My Test Library ${LocalDateTime.now()}",
                true,
                TrainingType.Companion.DEFAULT_LIST,
                platform,
                Platform.INTERVALS
            )
        )
        libraryService.deleteLibrary(DeleteLibraryRequest(response.externalData, Platform.INTERVALS))

        Assertions.assertEquals(response.copied, 5)
    }
}
