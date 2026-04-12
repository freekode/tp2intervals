package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FromTPStructureConverterTest {

    @Test
    fun `should convert single step`() {
        val step = TPStepDTO().apply {
            name = "Warmup"
            length = TPLengthDTO(300, "second")
            targets = listOf(TPTargetDTO.mainTarget(50, 75))
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(TPStructureStepDTO.singleStep(step)),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        val result = FromTPStructureConverter.toWorkoutStructure(structure)

        assertEquals(1, result.steps.size)
        val stepResult = result.steps[0] as org.freekode.tp2intervals.domain.workout.structure.SingleStep
        assertEquals("Warmup", stepResult.name)
        assertEquals(StepLength.seconds(300), stepResult.length)
        assertEquals(StepTarget(50, 75), stepResult.target)
    }

    @Test
    fun `should convert multi step with repetitions`() {
        val innerStep = TPStepDTO().apply {
            name = "Interval"
            length = TPLengthDTO(120, "second")
            targets = listOf(TPTargetDTO.mainTarget(100))
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(TPStructureStepDTO.multiStep(3, listOf(innerStep))),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        val result = FromTPStructureConverter.toWorkoutStructure(structure)

        assertEquals(1, result.steps.size)
        val multiStep = result.steps[0] as org.freekode.tp2intervals.domain.workout.structure.MultiStep
        assertEquals(3, multiStep.repetitions)
        assertEquals(1, multiStep.steps.size)
    }

    @Test
    fun `should convert rampUp step as multi step`() {
        val innerStep = TPStepDTO().apply {
            name = "Ramp"
            length = TPLengthDTO(60, "second")
            targets = listOf(TPTargetDTO.mainTarget(50))
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(
                TPStructureStepDTO(
                    type = "rampUp",
                    length = TPLengthDTO.repetitions(1),
                    steps = listOf(innerStep)
                )
            ),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        val result = FromTPStructureConverter.toWorkoutStructure(structure)

        assertEquals(1, result.steps.size)
        assert(result.steps[0] is org.freekode.tp2intervals.domain.workout.structure.MultiStep)
    }

    @Test
    fun `should convert rampDown step as multi step`() {
        val innerStep = TPStepDTO().apply {
            name = "Ramp"
            length = TPLengthDTO(60, "second")
            targets = listOf(TPTargetDTO.mainTarget(50))
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(
                TPStructureStepDTO(
                    type = "rampDown",
                    length = TPLengthDTO.repetitions(1),
                    steps = listOf(innerStep)
                )
            ),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        val result = FromTPStructureConverter.toWorkoutStructure(structure)

        assertEquals(1, result.steps.size)
        assert(result.steps[0] is org.freekode.tp2intervals.domain.workout.structure.MultiStep)
    }

    @Test
    fun `should throw for unknown step type`() {
        val structure = TPWorkoutStructureDTO(
            structure = listOf(
                TPStructureStepDTO(
                    type = "unknown",
                    length = null,
                    steps = emptyList()
                )
            ),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        assertThrows<IllegalArgumentException> {
            FromTPStructureConverter.toWorkoutStructure(structure)
        }
    }

    @Test
    fun `should throw when step type has no steps`() {
        val structure = TPWorkoutStructureDTO(
            structure = listOf(
                TPStructureStepDTO(
                    type = "step",
                    length = null,
                    steps = emptyList()
                )
            ),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        assertThrows<IllegalArgumentException> {
            FromTPStructureConverter.toWorkoutStructure(structure)
        }
    }

    @Test
    fun `should convert cadence target`() {
        val step = TPStepDTO().apply {
            name = "Interval"
            length = TPLengthDTO(120, "second")
            targets = listOf(
                TPTargetDTO.mainTarget(100),
                TPTargetDTO.cadenceTarget(90, 95)
            )
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(TPStructureStepDTO.singleStep(step)),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        val result = FromTPStructureConverter.toWorkoutStructure(structure)

        val stepResult = result.steps[0] as org.freekode.tp2intervals.domain.workout.structure.SingleStep
        assertEquals(StepTarget(90, 95), stepResult.cadence)
    }

    @Test
    fun `should map FTP intensity`() {
        val step = TPStepDTO().apply {
            name = "Interval"
            length = TPLengthDTO(120, "second")
            targets = listOf(TPTargetDTO.mainTarget(100))
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(TPStructureStepDTO.singleStep(step)),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "percentOfFtp",
            visualizationDistanceUnit = null
        )

        val result = FromTPStructureConverter.toWorkoutStructure(structure)

        assertEquals(
            org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure.TargetUnit.FTP_PERCENTAGE,
            result.target
        )
    }

    @Test
    fun `should throw for unknown intensity`() {
        val step = TPStepDTO().apply {
            name = "Interval"
            length = TPLengthDTO(120, "second")
            targets = listOf(TPTargetDTO.mainTarget(100))
        }
        val structure = TPWorkoutStructureDTO(
            structure = listOf(TPStructureStepDTO.singleStep(step)),
            primaryLengthMetric = "duration",
            primaryIntensityMetric = "unknown",
            visualizationDistanceUnit = null
        )

        assertThrows<IllegalArgumentException> {
            FromTPStructureConverter.toWorkoutStructure(structure)
        }
    }
}
