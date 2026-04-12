package org.freekode.tp2intervals.domain.workout.structure

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SingleStepTest {

    @Test
    fun `should identify as single step`() {
        val step = SingleStep(
            "Step",
            StepLength.seconds(60),
            StepTarget(75, 75),
            null,
            false
        )

        assert(step.isSingleStep())
    }

    @Test
    fun `should throw when converting non-ramp step`() {
        val step = SingleStep(
            "Step",
            StepLength.seconds(60),
            StepTarget(50, 100),
            null,
            false
        )

        assertThrows<IllegalStateException> {
            step.convertRampToMultiStep()
        }
    }

    @Test
    fun `should convert ramp step to multi step`() {
        val step = SingleStep(
            "Ramp",
            StepLength.seconds(180),
            StepTarget(50, 100),
            null,
            true
        )

        val result = step.convertRampToMultiStep()

        assertEquals("Ramp", result.name)
        assertEquals(1, result.repetitions)
        assertEquals(3, result.steps.size)
    }
}
