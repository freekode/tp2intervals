package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import config.mock.ObjectMapperFactory
import config.mock.TrainerRoadApiClientMock
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfiguration
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils
import java.time.Duration

class TrainerRoadWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val trainerRoadApiClient = TrainerRoadApiClientMock(
        objectMapper,
        ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json").inputStream(),
        ResourceUtils.getFile("classpath:tr-workoutsdetails-complex.json").inputStream(),
        ResourceUtils.getFile("classpath:tr-workoutsdetails-another.json").inputStream(),
    )

    private val trainerRoadConfigurationRepository = trainerRoadConfigurationRepository()

    private fun trainerRoadConfigurationRepository(): TrainerRoadConfigurationRepository {
        val mock = mock(TrainerRoadConfigurationRepository::class.java)
        `when`(mock.getConfiguration()).thenReturn(TrainerRoadConfiguration(null, true))
        return mock
    }

    private val trainerRoadApiClientService = TrainerRoadApiClientService(trainerRoadApiClient, trainerRoadConfigurationRepository)

    private val trainerRoadWorkoutRepository = TrainerRoadWorkoutRepository(mock(TRUsernameRepository::class.java), trainerRoadApiClientService)

    @Test
    fun `should parse simple workout`() {
        // when
        val data = ExternalData(null, null, "simple")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE)
        assertTrue(workout.structure!!.steps.size == 11)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(5))
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 50)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 50)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).duration == Duration.ofMinutes(3))
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 60)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 60)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).duration == Duration.ofMinutes(13))
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 50)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 50)
    }

    @Test
    fun `should parse complex workout`() {
        // when
        val data = ExternalData(null, null, "complex")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE)
        assertTrue(workout.structure!!.steps.size == 23)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(4))
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 50)
        assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 50)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).duration == Duration.ofMinutes(2))
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 55)
        assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 55)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).duration == Duration.ofMinutes(2))
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 60)
        assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 60)
    }

    @Test
    fun `should print wrong rest api response`() {
        // when
        val data = ExternalData(null, null, "another")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE)
        assertTrue(workout.structure!!.steps.isNotEmpty())
    }
}
