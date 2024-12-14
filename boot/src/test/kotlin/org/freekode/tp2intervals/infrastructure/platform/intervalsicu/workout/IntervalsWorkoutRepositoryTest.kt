package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import config.mock.IntervalsApiClientMock
import config.mock.ObjectMapperFactory
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.IntervalsApiClient
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import org.freekode.tp2intervals.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertTrue(workouts.size == 5)

        val workout = findWorkoutWithName("hr test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.LTHR_PERCENTAGE, structure.target)
        assertEquals(5, structure.steps.size)
        // 10m 50-70% LTHR
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(50, (structure.steps[0] as SingleStep).target.start)
        assertEquals(70, (structure.steps[0] as SingleStep).target.end)
        // 10m 80% LTHR
        assertEquals(78, (structure.steps[1] as SingleStep).target.start)
        assertEquals(82, (structure.steps[1] as SingleStep).target.end)
        // 10m ramp 45-60% LTHR
        assertEquals(45, (structure.steps[2] as SingleStep).target.start)
        assertEquals(60, (structure.steps[2] as SingleStep).target.end)
        (structure.steps[2] as SingleStep).ramp
        // 10m Z2 HR
        assertEquals(68, (structure.steps[3] as SingleStep).target.start)
        assertEquals(82, (structure.steps[3] as SingleStep).target.end)
        // 10m Z3-Z4 HR
        assertEquals(83, (structure.steps[4] as SingleStep).target.start)
        assertEquals(105, (structure.steps[4] as SingleStep).target.end)
    }

    @Test
    fun `should parse power workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("power test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.BIKE, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, structure.target)
        assertEquals(5, structure.steps.size)
        // 10m 10-30%
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(10, (structure.steps[0] as SingleStep).target.start)
        assertEquals(30, (structure.steps[0] as SingleStep).target.end)
        // 10m 40% 30-90rpm
        assertEquals(39, (structure.steps[1] as SingleStep).target.start)
        assertEquals(41, (structure.steps[1] as SingleStep).target.end)
        assertEquals(30, (structure.steps[1] as SingleStep).cadence!!.start)
        assertEquals(90, (structure.steps[1] as SingleStep).cadence!!.end)
        // 10m ramp 10-60%
        assertEquals(10, (structure.steps[2] as SingleStep).target.start)
        assertEquals(60, (structure.steps[2] as SingleStep).target.end)
        (structure.steps[2] as SingleStep).ramp
        // 10m Z2 85rpm
        assertEquals(56, (structure.steps[3] as SingleStep).target.start)
        assertEquals(75, (structure.steps[3] as SingleStep).target.end)
        assertEquals(85, (structure.steps[3] as SingleStep).cadence!!.start)
        assertEquals(85, (structure.steps[3] as SingleStep).cadence!!.end)
        // 10m Z3-Z4
        assertEquals(76, (structure.steps[4] as SingleStep).target.start)
        assertEquals(105, (structure.steps[4] as SingleStep).target.end)
    }

    @Test
    fun `should parse pace workout`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("pace test", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.RUN, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, structure.target)
        assertEquals(4, structure.steps.size)
        // 10m 70% Pace
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(68, (structure.steps[0] as SingleStep).target.start)
        assertEquals(72, (structure.steps[0] as SingleStep).target.end)
        // 10m 80-110% Pace
        assertEquals(80, (structure.steps[1] as SingleStep).target.start)
        assertEquals(110, (structure.steps[1] as SingleStep).target.end)
        // 10m Z2 Pace
        assertEquals(79, (structure.steps[2] as SingleStep).target.start)
        assertEquals(88, (structure.steps[2] as SingleStep).target.end)
        // 10m Z3-Z5 Pace
        assertEquals(89, (structure.steps[3] as SingleStep).target.start)
        assertEquals(103, (structure.steps[3] as SingleStep).target.end)
    }

    @Test
    fun `should parse pace workout with distance based steps`() {
        // when
        val workouts = intervalsWorkoutRepository.getWorkoutsFromCalendar(LocalDate.now(), LocalDate.now())

        // then
        assertEquals(5, workouts.size)

        val workout = findWorkoutWithName("distance based", workouts)
        val structure = workout.structure!!

        assertEquals(TrainingType.RUN, workout.details.type)
        assertEquals(WorkoutStructure.TargetUnit.PACE_PERCENTAGE, structure.target)
        assertEquals(4, structure.steps.size)
        // 10m 70% Pace
        assertEquals(600.toLong(), (structure.steps[0] as SingleStep).length.value)
        assertEquals(68, (structure.steps[0] as SingleStep).target.start)
        assertEquals(72, (structure.steps[0] as SingleStep).target.end)
        // 10m 80-110% Pace
        assertEquals(80, (structure.steps[1] as SingleStep).target.start)
        assertEquals(110, (structure.steps[1] as SingleStep).target.end)
        // 10m Z2 Pace
        assertEquals(79, (structure.steps[2] as SingleStep).target.start)
        assertEquals(88, (structure.steps[2] as SingleStep).target.end)
        // 10m Z3-Z5 Pace
        assertEquals(89, (structure.steps[3] as SingleStep).target.start)
        assertEquals(103, (structure.steps[3] as SingleStep).target.end)
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
