package org.freekode.tp2intervals.app.workout

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class WorkoutServiceTest {

    private val trainingPeaksRepo: WorkoutRepository = mockk()
    private val intervalsRepo: WorkoutRepository = mockk()
    private val trainingPeaksPlanRepo: LibraryContainerRepository = mockk()
    private val intervalsPlanRepo: LibraryContainerRepository = mockk()

    private lateinit var service: WorkoutService

    @BeforeEach
    fun setup() {
        every { trainingPeaksRepo.platform() } returns Platform.TRAINING_PEAKS
        every { intervalsRepo.platform() } returns Platform.INTERVALS
        every { trainingPeaksPlanRepo.platform() } returns Platform.TRAINING_PEAKS
        every { intervalsPlanRepo.platform() } returns Platform.INTERVALS

        service = WorkoutService(
            listOf(trainingPeaksRepo, intervalsRepo),
            listOf(trainingPeaksPlanRepo, intervalsPlanRepo)
        )
    }

    @Test
    fun `should copy workouts from calendar to calendar`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val workout1 = createWorkout(TrainingType.RIDE, "Workout 1")
        val workout2 = createWorkout(TrainingType.RUN, "Workout 2")
        every { trainingPeaksRepo.getWorkoutsFromCalendar(startDate, endDate) } returns listOf(workout1, workout2)
        every { intervalsRepo.getWorkoutsFromCalendar(startDate, endDate) } returns emptyList()

        val request = CopyFromCalendarToCalendarRequest(
            startDate, endDate,
            listOf(TrainingType.RIDE),
            skipSynced = false,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.copyWorkoutsC2C(request)

        assert(response.copied == 1)
        assert(response.filteredOut == 0)
        verify { intervalsRepo.saveWorkoutsToCalendar(listOf(workout1)) }
    }

    @Test
    fun `should skip already synced workouts when flag is set`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val workout1 = createWorkout(TrainingType.RIDE, "Workout 1")
        every { trainingPeaksRepo.getWorkoutsFromCalendar(startDate, endDate) } returns listOf(workout1)
        every { intervalsRepo.getWorkoutsFromCalendar(startDate, endDate) } returns listOf(workout1)

        val request = CopyFromCalendarToCalendarRequest(
            startDate, endDate,
            TrainingType.DEFAULT_LIST,
            skipSynced = true,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.copyWorkoutsC2C(request)

        assert(response.copied == 0)
        assert(response.filteredOut == 1)
    }

    @Test
    fun `should filter workouts by training type`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val rideWorkout = createWorkout(TrainingType.RIDE, "Ride")
        val runWorkout = createWorkout(TrainingType.RUN, "Run")
        every { trainingPeaksRepo.getWorkoutsFromCalendar(startDate, endDate) } returns listOf(rideWorkout, runWorkout)
        every { intervalsRepo.getWorkoutsFromCalendar(startDate, endDate) } returns emptyList()

        val request = CopyFromCalendarToCalendarRequest(
            startDate, endDate,
            listOf(TrainingType.RUN),
            skipSynced = false,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.copyWorkoutsC2C(request)

        assert(response.copied == 1)
        verify { intervalsRepo.saveWorkoutsToCalendar(match { it.first().details.name == "Run" }) }
    }

    @Test
    fun `should copy workouts from calendar to library`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val workout = createWorkout(TrainingType.RIDE, "Workout")
        val newLibrary = LibraryContainer("New Library", startDate, true, 1, ExternalData.empty())
        every { trainingPeaksRepo.getWorkoutsFromCalendar(startDate, endDate) } returns listOf(workout)
        every { intervalsPlanRepo.createLibraryContainer("My Library", true, startDate) } returns newLibrary

        val request = CopyFromCalendarToLibraryRequest(
            startDate, endDate,
            "My Library",
            isPlan = true,
            types = TrainingType.DEFAULT_LIST,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.copyWorkoutsC2L(request)

        assert(response.copied == 1)
        verify { intervalsRepo.saveWorkoutsToLibrary(newLibrary, listOf(workout)) }
    }

    @Test
    fun `should copy workouts from library to library`() {
        val workout = createWorkout(TrainingType.RIDE, "Workout")
        val targetLibrary = LibraryContainer("Target", LocalDate.now(), true, 1, ExternalData.empty())
        every { trainingPeaksRepo.getWorkoutFromLibrary(ExternalData.empty()) } returns workout

        val request = CopyFromLibraryToLibraryRequest(
            workoutExternalData = ExternalData.empty(),
            targetLibraryContainer = targetLibrary,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.copyWorkoutL2L(request)

        assert(response.copied == 1)
        verify { intervalsRepo.saveWorkoutsToLibrary(targetLibrary, listOf(workout)) }
    }

    @Test
    fun `should find workouts by name`() {
        val expectedDetails = WorkoutDetails(
            TrainingType.RIDE, "Evening Ride", null, null, null, ExternalData.empty()
        )
        every { trainingPeaksRepo.findWorkoutsFromLibraryByName("Evening") } returns listOf(expectedDetails)

        val result = service.findWorkoutsByName(Platform.TRAINING_PEAKS, "Evening")

        assert(result.size == 1)
        assert(result.first().name == "Evening Ride")
    }

    @Test
    fun `should delete workouts from calendar`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)

        service.deleteWorkoutsFromCalendar(
            org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO(
                startDate, endDate, Platform.INTERVALS
            )
        )

        verify { intervalsRepo.deleteWorkoutsFromCalendar(startDate, endDate) }
    }

    private fun createWorkout(type: TrainingType, name: String): Workout {
        return Workout(
            WorkoutDetails(
                type, name, null, null, null, ExternalData.empty()
            ),
            LocalDate.now(),
            null
        )
    }
}
