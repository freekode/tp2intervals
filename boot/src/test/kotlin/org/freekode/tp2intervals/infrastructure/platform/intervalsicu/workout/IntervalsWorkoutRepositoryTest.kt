package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import config.mock.IntervalsApiClientMock
import config.mock.ObjectMapperFactory
import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils

class IntervalsWorkoutRepositoryTest {
    private val objectMapper = ObjectMapperFactory.objectMapper()

    private val intervalsApiClient: IntervalsApiClient = IntervalsApiClientMock(
        objectMapper,
        ResourceUtils.getFile("classpath:intervals-events-response.json").inputStream()
    )

    private val intervalsConfigurationRepository: IntervalsConfigurationRepository =
        getIntervalsConfigurationRepository()

    private val intervalsWorkoutRepository =
        IntervalsWorkoutRepository(intervalsApiClient, intervalsConfigurationRepository)

    @Test
    fun `should parse hr workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        Assertions.assertTrue(workouts.size == 5)

        val workout = findWorkoutWithName("hr test", workouts)
        Assertions.assertTrue(workout.details.type == TrainingType.BIKE)
        Assertions.assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.LTHR_PERCENTAGE)
        Assertions.assertTrue(workout.structure!!.steps.size == 5)
        // 10m 50-70% LTHR
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10))
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 50)
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 70)
        // 10m 80% LTHR
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 78)
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 82)
        // 10m ramp 45-60% LTHR
        Assertions.assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 45)
        Assertions.assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 60)
        (workout.structure!!.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 HR
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).target.start == 68)
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).target.end == 82)
        // 10m Z3-Z4 HR
        Assertions.assertTrue((workout.structure!!.steps[4] as WorkoutSingleStep).target.start == 83)
        Assertions.assertTrue((workout.structure!!.steps[4] as WorkoutSingleStep).target.end == 105)
    }

    @Test
    fun `should parse power workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        Assertions.assertTrue(workouts.size == 5)

        val workout = findWorkoutWithName("power test", workouts)
        Assertions.assertTrue(workout.details.type == TrainingType.BIKE)
        Assertions.assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE)
        Assertions.assertTrue(workout.structure!!.steps.size == 5)
        // 10m 10-30%
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10))
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 10)
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 30)
        // 10m 40% 30-90rpm
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 39)
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 41)
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).cadence!!.start == 30)
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).cadence!!.end == 90)
        // 10m ramp 10-60%
        Assertions.assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 10)
        Assertions.assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 60)
        (workout.structure!!.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 85rpm
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).target.start == 56)
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).target.end == 75)
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).cadence!!.start == 85)
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).cadence!!.end == 85)
        // 10m Z3-Z4
        Assertions.assertTrue((workout.structure!!.steps[4] as WorkoutSingleStep).target.start == 76)
        Assertions.assertTrue((workout.structure!!.steps[4] as WorkoutSingleStep).target.end == 105)
    }

    @Test
    fun `should parse pace workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        Assertions.assertTrue(workouts.size == 5)

        val workout = findWorkoutWithName("pace test", workouts)
        Assertions.assertTrue(workout.details.type == TrainingType.RUN)
        Assertions.assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.PACE_PERCENTAGE)
        Assertions.assertTrue(workout.structure!!.steps.size == 4)
        // 10m 70% Pace
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).duration == Duration.ofMinutes(10))
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.start == 68)
        Assertions.assertTrue((workout.structure!!.steps[0] as WorkoutSingleStep).target.end == 72)
        // 10m 80-110% Pace
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.start == 80)
        Assertions.assertTrue((workout.structure!!.steps[1] as WorkoutSingleStep).target.end == 110)
        // 10m Z2 Pace
        Assertions.assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.start == 79)
        Assertions.assertTrue((workout.structure!!.steps[2] as WorkoutSingleStep).target.end == 88)
        // 10m Z3-Z5 Pace
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).target.start == 89)
        Assertions.assertTrue((workout.structure!!.steps[3] as WorkoutSingleStep).target.end == 103)
    }

    @Test
    fun `should parse virtual ride workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        Assertions.assertTrue(workouts.size == 5)

        val workout = findWorkoutWithName("virtual ride test", workouts)
        Assertions.assertTrue(workout.details.type == TrainingType.VIRTUAL_BIKE)
        Assertions.assertTrue(workout.structure!!.target == WorkoutStructure.TargetUnit.FTP_PERCENTAGE)
        Assertions.assertTrue(workout.structure!!.steps.size == 5)
    }

    @Test
    fun `should parse other workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        Assertions.assertTrue(workouts.size == 5)

        val workout = findWorkoutWithName("other test", workouts)
        Assertions.assertTrue(workout.details.type == TrainingType.UNKNOWN)
        Assertions.assertTrue(workout.details.duration == Duration.ofMinutes(45))
        Assertions.assertTrue(workout.details.load == 32)
        Assertions.assertTrue(workout.structure == null)
    }

    private fun findWorkoutWithName(name: String, workouts: List<Workout>): Workout {
        return workouts.find { it.details.name == name }!!
    }

    private fun getIntervalsConfigurationRepository(): IntervalsConfigurationRepository {
        val repo = mock(IntervalsConfigurationRepository::class.java)
        `when`(repo.getConfiguration()).thenReturn(IntervalsConfiguration("apiKey", "athleteId", 0.1f, 0.2f, 0.3f))
        return repo
    }
}
