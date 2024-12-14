package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import config.mock.ObjectMapperFactory
import config.mock.TrainingPeaksApiClientMock
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanCoachApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUser
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertTrue


class TrainingPeaksWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val trainingPeaksApiClient = TrainingPeaksApiClientMock(
        objectMapper,
        ResourceUtils.getFile("classpath:tp-calendar-workout-swim.json").inputStream(),
    )

    private val trainingPeaksUserRepository = getTrainingPeaksUserRepository()

    private val trainingPeaksWorkoutRepository = TrainingPeaksWorkoutRepository(
        trainingPeaksApiClient,
        mock(TrainingPeaksPlanCoachApiClient::class.java),
        TPToWorkoutConverter(),
        mock(TrainingPeaksPlanRepository::class.java),
        trainingPeaksUserRepository,
        mock(TPWorkoutLibraryRepository::class.java),
        mock(TrainingPeaksConfigurationRepository::class.java),
        objectMapper
    )

    @Test
    fun go() {
        val workoutsFromCalendar =
            trainingPeaksWorkoutRepository.getWorkoutsFromCalendar(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1))

        assertTrue(workoutsFromCalendar.isNotEmpty())
    }

    private fun getTrainingPeaksUserRepository(): TrainingPeaksUserRepository {
        val mock = mock(TrainingPeaksUserRepository::class.java)
        `when`(mock.getUser()).thenReturn(TrainingPeaksUser("123", true))
        return mock
    }
}
