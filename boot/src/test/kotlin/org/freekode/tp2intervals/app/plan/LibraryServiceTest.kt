package org.freekode.tp2intervals.app.plan

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
import org.freekode.tp2intervals.domain.workout.structure.StepModifier
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LibraryServiceTest {

    private val trainingPeaksRepo: WorkoutRepository = mockk(relaxed = true)
    private val intervalsRepo: WorkoutRepository = mockk(relaxed = true)
    private val trainingPeaksPlanRepo: LibraryContainerRepository = mockk(relaxed = true)
    private val intervalsPlanRepo: LibraryContainerRepository = mockk(relaxed = true)

    private lateinit var service: LibraryService

    @BeforeEach
    fun setup() {
        every { trainingPeaksRepo.platform() } returns Platform.TRAINING_PEAKS
        every { intervalsRepo.platform() } returns Platform.INTERVALS
        every { trainingPeaksPlanRepo.platform() } returns Platform.TRAINING_PEAKS
        every { intervalsPlanRepo.platform() } returns Platform.INTERVALS

        service = LibraryService(
            listOf(trainingPeaksRepo, intervalsRepo),
            listOf(trainingPeaksPlanRepo, intervalsPlanRepo)
        )
    }

    @Test
    fun `should find libraries by platform`() {
        val library = LibraryContainer("My Plan", LocalDate.now(), true, 10, ExternalData.empty())
        every { trainingPeaksPlanRepo.getLibraryContainers() } returns listOf(library)

        val result = service.findByPlatform(Platform.TRAINING_PEAKS)

        assert(result.size == 1)
        assert(result.first().name == "My Plan")
    }

    @Test
    fun `should copy library with workouts`() {
        val sourceLibrary = LibraryContainer("Source Plan", LocalDate.now(), true, 5, ExternalData.empty())
        val targetLibrary = LibraryContainer("Target Plan", LocalDate.now(), true, 3, ExternalData.empty())
        val workout1 = createWorkout("Workout 1", LocalDate.now())
        val workout2 = createWorkout("Workout 2", LocalDate.now().plusDays(1))
        every { trainingPeaksRepo.getWorkoutsFromLibrary(sourceLibrary) } returns listOf(workout1, workout2)
        every { intervalsPlanRepo.createLibraryContainer("New Plan", true, LocalDate.now()) } returns targetLibrary

        val request = CopyLibraryRequest(
            libraryContainer = sourceLibrary,
            newName = "New Plan",
            stepModifier = StepModifier.NONE,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        val response = service.copyLibrary(request)

        assert(response.planName == "Target Plan")
        assert(response.workouts == 2)
        verify { intervalsRepo.saveWorkoutsToLibrary(targetLibrary, match { it.size == 2 }) }
    }

    @Test
    fun `should delete library`() {
        val externalData = ExternalData.empty()

        service.deleteLibrary(DeleteLibraryRequest(externalData, Platform.INTERVALS))

        verify { intervalsPlanRepo.deleteLibraryContainer(externalData) }
    }

    @Test
    fun `should create library container`() {
        val library = LibraryContainer("New Library", LocalDate.now(), false, 0, ExternalData.empty())
        every { intervalsPlanRepo.createLibraryContainer("New Library", false, null) } returns library

        val result = service.create(CreateLibraryContainerRequest("New Library", Platform.INTERVALS))

        assert(result.name == "New Library")
        assert(result.isPlan == false)
    }

    @Test
    fun `should apply step modifier when copying library`() {
        val sourceLibrary = LibraryContainer("Source", LocalDate.now(), true, 1, ExternalData.empty())
        val targetLibrary = LibraryContainer("Target", LocalDate.now(), true, 1, ExternalData.empty())
        val workout = createWorkout("Workout", LocalDate.now())
        every { trainingPeaksRepo.getWorkoutsFromLibrary(sourceLibrary) } returns listOf(workout)
        every { intervalsPlanRepo.createLibraryContainer(any(), any(), any()) } returns targetLibrary

        val request = CopyLibraryRequest(
            libraryContainer = sourceLibrary,
            newName = "New",
            stepModifier = StepModifier.POWER_10S,
            sourcePlatform = Platform.TRAINING_PEAKS,
            targetPlatform = Platform.INTERVALS
        )
        service.copyLibrary(request)

        verify { intervalsRepo.saveWorkoutsToLibrary(targetLibrary, match { it.first().structure?.modifier == StepModifier.POWER_10S }) }
    }

    private fun createWorkout(name: String, date: LocalDate): Workout {
        return Workout(
            WorkoutDetails(
                TrainingType.BIKE, name, null, null, null, ExternalData.empty()
            ),
            date,
            null
        )
    }
}
