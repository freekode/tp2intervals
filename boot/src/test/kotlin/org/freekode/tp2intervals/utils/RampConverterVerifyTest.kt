package org.freekode.tp2intervals.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RampConverterVerifyTest {

    @Test
    fun `should convert ramp to multi-step structure`() {
        val step = mockk<SingleStep> {
            every { length } returns StepLength.seconds(300)
            every { target } returns StepTarget(50, 100)
            every { cadence } returns null
            every { ramp } returns true
            every { name } returns "Test Ramp"
        }

        val result = RampConverter(step).toRampToMultiStep()

        assertEquals(5, result.steps.size)
    }

    @Test
    fun `should throw when step is not time-based`() {
        val step = mockk<SingleStep> {
            every { length } returns StepLength(1000, StepLength.LengthUnit.METERS)
            every { target } returns StepTarget(50, 100)
            every { ramp } returns true
            every { name } returns "Test"
        }

        assertThrows(IllegalStateException::class.java) {
            RampConverter(step).toRampToMultiStep()
        }
    }
}
