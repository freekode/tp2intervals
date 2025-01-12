package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import config.TestUtils
import config.mock.ObjectMapperFactory
import config.mock.TrainingPeaksApiClientMock
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.MultiStep
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanCoachApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUser
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils
import java.time.LocalDate


class TrainingPeaksWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val trainingPeaksUserRepository = trainingPeaksUserRepository()

    @Test
    fun `should parse swim workout with distance based steps`() {
        // when
        val trainingPeaksApiClient = TrainingPeaksApiClientMock(
            objectMapper,
            ResourceUtils.getFile("classpath:tp-calendar-workout-distant-steps.json").inputStream()
        )
        val trainingPeaksWorkoutRepository = trainingPeaksWorkoutRepository(trainingPeaksApiClient)
        val workouts =
            trainingPeaksWorkoutRepository.getWorkoutsFromCalendar(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1))

        // then
        assertTrue(workouts.isNotEmpty())

        val workout = workouts[0]
        val structure = workout.structure!!

        assertEquals(TrainingType.SWIM, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, structure.target)
        assertEquals(8, structure.steps.size)

        val multiStep1 = (structure.steps[0] as MultiStep)
        assertEquals(3, multiStep1.steps.size)
        TestUtils.assertStep(multiStep1.steps[0], 50, StepLength.LengthUnit.METERS, 75, 75)

        TestUtils.assertStep(structure.steps[1] as SingleStep, 10, StepLength.LengthUnit.SECONDS, 0, 0)
        TestUtils.assertStep(structure.steps[3] as SingleStep, 200, StepLength.LengthUnit.METERS, 75, 75)

        val multiStep2 = (structure.steps[4] as MultiStep)
        assertEquals(2, multiStep2.steps.size)
        TestUtils.assertStep(multiStep2.steps[0], 50, StepLength.LengthUnit.METERS, 90, 90)
        TestUtils.assertStep(multiStep2.steps[1], 10, StepLength.LengthUnit.SECONDS, 0, 0)
    }

    @Test
    fun `should parse workout with null title`() {
        // when
        val trainingPeaksApiClient = TrainingPeaksApiClientMock(
            objectMapper,
            ResourceUtils.getFile("classpath:tp-calendar-workout-null-title.json").inputStream()
        )
        val trainingPeaksWorkoutRepository = trainingPeaksWorkoutRepository(trainingPeaksApiClient)
        val workouts =
            trainingPeaksWorkoutRepository.getWorkoutsFromCalendar(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1))

        // then
        assertTrue(workouts.isNotEmpty())

        val workout = workouts[0]
        val structure = workout.structure!!

        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(10, structure.steps.size)
    }

    private fun trainingPeaksUserRepository(): TrainingPeaksUserRepository {
        val mock = mock(TrainingPeaksUserRepository::class.java)
        `when`(mock.getUser()).thenReturn(TrainingPeaksUser("123", true, false))
        return mock
    }

    private fun trainingPeaksWorkoutRepository(trainingPeaksApiClient: TrainingPeaksApiClient) =
        TrainingPeaksWorkoutRepository(
            trainingPeaksApiClient,
            mock(TrainingPeaksPlanCoachApiClient::class.java),
            TPToWorkoutConverter(),
            mock(TrainingPeaksPlanRepository::class.java),
            trainingPeaksUserRepository,
            mock(TPWorkoutLibraryRepository::class.java),
            mock(TrainingPeaksConfigurationRepository::class.java),
            objectMapper
        )
}
