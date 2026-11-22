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
        TrainerRoadWorkoutRepository(
            mock(TRUsernameRepository::class.java),
            trainerRoadApiClientService,
            trainerRoadConfigurationRepository,
        )

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
    }

    @Test
    fun `should use WorkoutData targets instead of zero interval fallbacks`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "265566",
                  "WorkoutName": "Galena -2",
                  "WorkoutDescription": "Synthetic workout",
                  "IsOutside": false,
                  "Tss": 72,
                  "Duration": 60
                },
                "IntervalData": [
                  {
                    "Start": 720,
                    "End": 1920,
                    "Name": "Sweet Spot 2",
                    "IsFake": false,
                    "TestInterval": false,
                    "StartTargetPowerPercent": 0,
                    "EndTargetPowerPercent": 0
                  }
                ],
                "WorkoutData": [
                  {"Tick": 720, "FtpPercent": 90},
                  {"Tick": 1919, "FtpPercent": 94}
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
    }

    @Test
    fun `should preserve workout without interval steps`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "planned-no-steps",
                  "WorkoutName": "Planned workout",
                  "WorkoutDescription": "Synthetic unstructured workout",
                  "IsOutside": false,
                  "Tss": 40,
                  "Duration": 45
                },
                "IntervalData": []
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(dto, true)

        assertEquals("Planned workout", workout.details.name)
        assertEquals(null, workout.structure)
    }

    @Test
    fun `should parse PascalCase startTarget range`() {
        val dto = objectMapper.readValue(
            """
            {
              "Workout": {
                "Details": {
                  "Id": "ramp-target",
                  "WorkoutName": "Ramped workout",
                  "WorkoutDescription": "Synthetic ramp",
                  "IsOutside": false,
                  "Tss": 50,
                  "Duration": 30
                },
                "IntervalData": [
                  {
                    "Start": 0,
                    "End": 600,
                    "Name": "Ramp",
                    "IsFake": false,
                    "TestInterval": false,
                    "StartTarget": [60, 90]
                  }
                ]
              }
            }
            """.trimIndent(),
            TRWorkoutResponseDTO::class.java,
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(dto, true)
        val step = workout.structure!!.steps.single() as SingleStep

        assertEquals(60, step.target.start)
        assertEquals(90, step.target.end)
    }

    private fun trainerRoadConfigurationRepository(): TrainerRoadConfigurationRepository {
        val mock = mock(TrainerRoadConfigurationRepository::class.java)
        `when`(mock.getConfiguration()).thenReturn(TrainerRoadConfiguration(null, true))
        return mock
    }
}
