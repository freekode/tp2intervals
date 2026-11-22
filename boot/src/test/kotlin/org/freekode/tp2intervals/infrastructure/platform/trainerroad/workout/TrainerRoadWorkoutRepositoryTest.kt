package org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout

import config.mock.ObjectMapperFactory
import config.mock.TrainerRoadApiClientMock
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.TrainerRoadApiClientService
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import org.freekode.tp2intervals.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
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
        val structure = workout.structure!!

        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(11, structure.steps.size)
        assertEquals(5 * 60.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(50, (structure.steps[0] as SingleStep).target.start)
        assertEquals(50, (structure.steps[0] as SingleStep).target.end)
        assertEquals(3 * 60.toLong(), (structure.steps[1] as SingleStep).length.value)
        assertEquals(60, (structure.steps[1] as SingleStep).target.start)
        assertEquals(60, (structure.steps[1] as SingleStep).target.end)
        assertEquals(13 * 60.toLong(), (structure.steps[2] as SingleStep).length.value)
        assertEquals(50, (structure.steps[2] as SingleStep).target.start)
        assertEquals(50, (structure.steps[2] as SingleStep).target.end)
    }

    @Test
    fun `should parse complex workout`() {
        // when
        val data = ExternalData(null, null, "complex")
        val workout = trainerRoadWorkoutRepository.getWorkoutFromLibrary(data)

        // then
        val structure = workout.structure!!

        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(23, structure.steps.size)
        assertEquals(4 * 60.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(50, (structure.steps[0] as SingleStep).target.start)
        assertEquals(50, (structure.steps[0] as SingleStep).target.end)
        assertEquals(2 * 60.toLong(), (structure.steps[1] as SingleStep).length.value)
        assertEquals(55, (structure.steps[1] as SingleStep).target.start)
        assertEquals(55, (structure.steps[1] as SingleStep).target.end)
        assertEquals(2 * 60.toLong(), (structure.steps[2] as SingleStep).length.value)
        assertEquals(60, (structure.steps[2] as SingleStep).target.start)
        assertEquals(60, (structure.steps[2] as SingleStep).target.end)
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
        assertTrue(workout.details.description!!.startsWith(
            "simple is 4x3-minute intervals of leg-speed drills at a very low 60% FTP"
        ))
        assertTrue(workout.details.description!!.contains("Goal:"))
        assertTrue(workout.details.description!!.contains("Zone: Endurance"))
        assertTrue(workout.details.description!!.contains("Progression: Endurance"))
    }

    @Test
    fun `should use live WorkoutData values when interval target fields are zero`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "265566",
                  "WorkoutName": "Galena -2",
                  "WorkoutDescription": "Sweet Spot workout",
                  "IsOutside": false,
                  "Tss": 70,
                  "Duration": 45
                },
                "IntervalData": [
                  {
                    "Start": 120,
                    "End": 1320,
                    "Name": "Sweet Spot 2",
                    "IsFake": false,
                    "TestInterval": false,
                    "StartTargetPowerPercent": 0,
                    "EndTargetPowerPercent": 0
                  }
                ],
                "WorkoutData": [
                  {"Tick": 120, "FtpPercent": 90},
                  {"Tick": 1319, "FtpPercent": 94}
                ]
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(dto, true)
        val step = workout.structure!!.steps.single() as SingleStep

        assertEquals(90, step.target.start)
        assertEquals(94, step.target.end)
        assertEquals(20 * 60.toLong(), step.length.value)
    }

    private fun trainerRoadConfigurationRepository(): TrainerRoadConfigurationRepository {
        val mock = mock(TrainerRoadConfigurationRepository::class.java)
        `when`(mock.getConfiguration()).thenReturn(TrainerRoadConfiguration(null, true))
        return mock
    }
}
