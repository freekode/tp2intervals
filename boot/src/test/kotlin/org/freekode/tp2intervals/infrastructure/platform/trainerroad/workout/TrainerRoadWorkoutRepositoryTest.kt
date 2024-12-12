package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import config.mock.ObjectMapperFactory
import config.mock.TrainerRoadApiClientMock
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClientService
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfiguration
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils

class TrainerRoadWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val trainerRoadApiClient = TrainerRoadApiClientMock(
        objectMapper,
        ResourceUtils.getFile("classpath:tr-workoutsdetails-simple.json").inputStream(),
        ResourceUtils.getFile("classpath:tr-workoutsdetails-complex.json").inputStream(),
        ResourceUtils.getFile("classpath:tr-workoutsdetails-another.json").inputStream(),
    )

    private val trainerRoadConfigurationRepository = trainerRoadConfigurationRepository()

    private val trainerRoadApiClientService =
        TrainerRoadApiClientService(trainerRoadApiClient, trainerRoadConfigurationRepository)

    private val trainerRoadWorkoutRepository =
        TrainerRoadWorkoutRepository(mock(TRUsernameRepository::class.java), trainerRoadApiClientService)

    @Test
    fun `should parse simple workout`() {
        // when
        val data = ExternalData(null, null, "simple")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertEquals(11, workout.structure!!.steps.size)
        assertEquals(5 * 60.toLong(), (workout.structure!!.steps[0] as WorkoutSingleStep).length.value)
        assertEquals(50, (workout.structure!!.steps[0] as WorkoutSingleStep).target.start)
        assertEquals(50, (workout.structure!!.steps[0] as WorkoutSingleStep).target.end)
        assertEquals(3 * 60.toLong(), (workout.structure!!.steps[1] as WorkoutSingleStep).length.value)
        assertEquals(60, (workout.structure!!.steps[1] as WorkoutSingleStep).target.start)
        assertEquals(60, (workout.structure!!.steps[1] as WorkoutSingleStep).target.end)
        assertEquals(13 * 60.toLong(), (workout.structure!!.steps[2] as WorkoutSingleStep).length.value)
        assertEquals(50, (workout.structure!!.steps[2] as WorkoutSingleStep).target.start)
        assertEquals(50, (workout.structure!!.steps[2] as WorkoutSingleStep).target.end)
    }

    @Test
    fun `should parse complex workout`() {
        // when
        val data = ExternalData(null, null, "complex")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertEquals(23, workout.structure!!.steps.size)
        assertEquals(4 * 60.toLong(), (workout.structure!!.steps[0] as WorkoutSingleStep).length.value)
        assertEquals(50, (workout.structure!!.steps[0] as WorkoutSingleStep).target.start)
        assertEquals(50, (workout.structure!!.steps[0] as WorkoutSingleStep).target.end)
        assertEquals(2 * 60.toLong(), (workout.structure!!.steps[1] as WorkoutSingleStep).length.value)
        assertEquals(55, (workout.structure!!.steps[1] as WorkoutSingleStep).target.start)
        assertEquals(55, (workout.structure!!.steps[1] as WorkoutSingleStep).target.end)
        assertEquals(2 * 60.toLong(), (workout.structure!!.steps[2] as WorkoutSingleStep).length.value)
        assertEquals(60, (workout.structure!!.steps[2] as WorkoutSingleStep).target.start)
        assertEquals(60, (workout.structure!!.steps[2] as WorkoutSingleStep).target.end)
    }

    @Test
    fun `should print wrong rest api response`() {
        // when
        val data = ExternalData(null, null, "another")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertTrue(workout.structure!!.steps.isNotEmpty())
    }

    @Test
    fun `should exclude html tags from description`() {
        val data = ExternalData(null, null, "simple")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)
        assertEquals(
            "simple is 4x3-minute intervals of leg-speed drills at a very low 60% FTP with 3 minutes of rest between intervals. " +
                    "Keep the pressure on the pedals light and your intensity low to moderate regardless of your cadence.",
            workout.details.description
        )
    }

    private fun trainerRoadConfigurationRepository(): TrainerRoadConfigurationRepository {
        val mock = mock(TrainerRoadConfigurationRepository::class.java)
        `when`(mock.getConfiguration()).thenReturn(TrainerRoadConfiguration(null, true))
        return mock
    }
}
