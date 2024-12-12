package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import config.mock.IntervalsApiClientMock
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.util.ResourceUtils
import java.time.Duration
import java.time.LocalDate

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
        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.LTHR_PERCENTAGE, workout.structure!!.target)
        assertEquals(5, workout.structure!!.steps.size)
        // 10m 50-70% LTHR
        assertEquals(600.toLong(), (workout.structure!!.steps[0] as WorkoutSingleStep).length.value)
        assertEquals(50, (workout.structure!!.steps[0] as WorkoutSingleStep).target.start)
        assertEquals(70, (workout.structure!!.steps[0] as WorkoutSingleStep).target.end)
        // 10m 80% LTHR
        assertEquals(78, (workout.structure!!.steps[1] as WorkoutSingleStep).target.start)
        assertEquals(82, (workout.structure!!.steps[1] as WorkoutSingleStep).target.end)
        // 10m ramp 45-60% LTHR
        assertEquals(45, (workout.structure!!.steps[2] as WorkoutSingleStep).target.start)
        assertEquals(60, (workout.structure!!.steps[2] as WorkoutSingleStep).target.end)
        (workout.structure!!.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 HR
        assertEquals(68, (workout.structure!!.steps[3] as WorkoutSingleStep).target.start)
        assertEquals(82, (workout.structure!!.steps[3] as WorkoutSingleStep).target.end)
        // 10m Z3-Z4 HR
        assertEquals(83, (workout.structure!!.steps[4] as WorkoutSingleStep).target.start)
        assertEquals(105, (workout.structure!!.steps[4] as WorkoutSingleStep).target.end)
    }

    @Test
    fun `should parse power workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("power test", workouts)
        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertEquals(5, workout.structure!!.steps.size)
        // 10m 10-30%
        assertEquals(600.toLong(), (workout.structure!!.steps[0] as WorkoutSingleStep).length.value)
        assertEquals(10, (workout.structure!!.steps[0] as WorkoutSingleStep).target.start)
        assertEquals(30, (workout.structure!!.steps[0] as WorkoutSingleStep).target.end)
        // 10m 40% 30-90rpm
        assertEquals(39, (workout.structure!!.steps[1] as WorkoutSingleStep).target.start)
        assertEquals(41, (workout.structure!!.steps[1] as WorkoutSingleStep).target.end)
        assertEquals(30, (workout.structure!!.steps[1] as WorkoutSingleStep).cadence!!.start)
        assertEquals(90, (workout.structure!!.steps[1] as WorkoutSingleStep).cadence!!.end)
        // 10m ramp 10-60%
        assertEquals(10, (workout.structure!!.steps[2] as WorkoutSingleStep).target.start)
        assertEquals(60, (workout.structure!!.steps[2] as WorkoutSingleStep).target.end)
        (workout.structure!!.steps[2] as WorkoutSingleStep).ramp
        // 10m Z2 85rpm
        assertEquals(56, (workout.structure!!.steps[3] as WorkoutSingleStep).target.start)
        assertEquals(75, (workout.structure!!.steps[3] as WorkoutSingleStep).target.end)
        assertEquals(85, (workout.structure!!.steps[3] as WorkoutSingleStep).cadence!!.start)
        assertEquals(85, (workout.structure!!.steps[3] as WorkoutSingleStep).cadence!!.end)
        // 10m Z3-Z4
        assertEquals(76, (workout.structure!!.steps[4] as WorkoutSingleStep).target.start)
        assertEquals(105, (workout.structure!!.steps[4] as WorkoutSingleStep).target.end)
    }

    @Test
    fun `should parse pace workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("pace test", workouts)
        assertEquals(TrainingType.RUN, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, workout.structure!!.target)
        assertEquals(4, workout.structure!!.steps.size)
        // 10m 70% Pace
        assertEquals(600.toLong(), (workout.structure!!.steps[0] as WorkoutSingleStep).length.value)
        assertEquals(68, (workout.structure!!.steps[0] as WorkoutSingleStep).target.start)
        assertEquals(72, (workout.structure!!.steps[0] as WorkoutSingleStep).target.end)
        // 10m 80-110% Pace
        assertEquals(80, (workout.structure!!.steps[1] as WorkoutSingleStep).target.start)
        assertEquals(110, (workout.structure!!.steps[1] as WorkoutSingleStep).target.end)
        // 10m Z2 Pace
        assertEquals(79, (workout.structure!!.steps[2] as WorkoutSingleStep).target.start)
        assertEquals(88, (workout.structure!!.steps[2] as WorkoutSingleStep).target.end)
        // 10m Z3-Z5 Pace
        assertEquals(89, (workout.structure!!.steps[3] as WorkoutSingleStep).target.start)
        assertEquals(103, (workout.structure!!.steps[3] as WorkoutSingleStep).target.end)
    }

    @Test
    fun `should parse virtual ride workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("virtual ride test", workouts)
        assertEquals(TrainingType.VIRTUAL_BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, workout.structure!!.target)
        assertEquals(5, workout.structure!!.steps.size)
    }

    @Test
    fun `should parse other workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("other test", workouts)
        assertEquals(TrainingType.UNKNOWN, workout.details.type)
        assertEquals(Duration.ofMinutes(45), workout.details.duration)
        assertEquals(32, workout.details.load)
        assertEquals(null, workout.structure)
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
