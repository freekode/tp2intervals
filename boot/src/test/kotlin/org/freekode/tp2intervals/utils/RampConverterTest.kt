package org.freekode.tp2intervals.utils

import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RampConverterTest {

    @Test
    fun `should throw when step is not time-based`() {
        val step = SingleStep(
            "Test",
            StepLength(1000, StepLength.LengthUnit.METERS),
            StepTarget(50, 100),
            null,
            true
        )

        assertThrows<IllegalStateException> {
            RampConverter(step).toRampToMultiStep()
        }
    }

    @Test
    fun `should convert 5 minute ramp to 60s steps`() {
        val step = SingleStep(
            "Ramp 5min",
            StepLength(300, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals("Ramp 5min", result.name)
        assertEquals(5, result.steps.size)
        result.steps.forEach { s ->
            assertEquals(60L, s.length.value)
            assertEquals(StepLength.LengthUnit.SECONDS, s.length.unit)
        }
    }

    @Test
    fun `should convert 10 minute ramp to 60s steps`() {
        val step = SingleStep(
            "Ramp 10min",
            StepLength(600, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(10, result.steps.size)
        result.steps.forEach { s ->
            assertEquals(60L, s.length.value)
        }
    }

    @Test
    fun `should convert 12 minute ramp to 120s steps`() {
        val step = SingleStep(
            "Ramp 12min",
            StepLength(720, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(6, result.steps.size)
        result.steps.forEach { s ->
            assertEquals(120L, s.length.value)
        }
    }

    @Test
    fun `should convert 15 minute ramp to 120s steps`() {
        val step = SingleStep(
            "Ramp 15min",
            StepLength(900, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(7, result.steps.size)
        result.steps.forEach { s ->
            assertEquals(120L, s.length.value)
        }
    }

    @Test
    fun `should convert 20 minute ramp to 180s steps`() {
        val step = SingleStep(
            "Ramp 20min",
            StepLength(1200, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(6, result.steps.size)
        result.steps.forEach { s ->
            assertEquals(180L, s.length.value)
        }
    }

    @Test
    fun `should distribute targets evenly across ramp steps`() {
        val step = SingleStep(
            "Ramp 3min",
            StepLength(180, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 80),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(3, result.steps.size)
        assertEquals(StepTarget(50, 60), result.steps[0].target)
        assertEquals(StepTarget(60, 70), result.steps[1].target)
        assertEquals(StepTarget(70, 80), result.steps[2].target)
    }

    @Test
    fun `should handle single value target`() {
        val step = SingleStep(
            "Ramp 3min",
            StepLength(180, StepLength.LengthUnit.SECONDS),
            StepTarget(75, 75),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(3, result.steps.size)
        result.steps.forEach { s ->
            assertEquals(StepTarget(75, 75), s.target)
        }
    }

    @Test
    fun `should handle remainder in duration`() {
        val step = SingleStep(
            "Ramp 5min30s",
            StepLength(330, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(6, result.steps.size)
        assertEquals(60L, result.steps[0].length.value)
        assertEquals(30L, result.steps[5].length.value)
    }

    @Test
    fun `should preserve cadence on ramp steps`() {
        val cadence = StepTarget(90, 95)
        val step = SingleStep(
            "Ramp",
            StepLength(180, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            cadence,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        result.steps.forEach { s ->
            assertEquals(cadence, s.cadence)
        }
    }

    @Test
    fun `should set ramp flag to false on converted steps`() {
        val step = SingleStep(
            "Ramp",
            StepLength(180, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        result.steps.forEach { s ->
            assertEquals(false, s.ramp)
        }
    }

    @Test
    fun `should set step name to Ramp`() {
        val step = SingleStep(
            "Original Name",
            StepLength(180, StepLength.LengthUnit.SECONDS),
            StepTarget(50, 100),
            null,
            true
        )

        val result = RampConverter(step).toRampToMultiStep()

        result.steps.forEach { s ->
            assertEquals("Ramp", s.name)
        }
    }
}
