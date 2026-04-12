package org.freekode.tp2intervals.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.StepTarget
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RampConverterMockkTest {

    @Test
    fun `should convert ramp with mockk`() {
        val step = mockk<SingleStep> {
            every { length } returns StepLength(300, StepLength.LengthUnit.SECONDS)
            every { target } returns StepTarget(50, 100)
            every { cadence } returns null
            every { ramp } returns true
            every { name } returns "Test Ramp"
        }

        val result = RampConverter(step).toRampToMultiStep()

        expectThat(result.steps.size).isEqualTo(5)
        verify(exactly = 1) { step.length }
    }

    @Test
    fun `should throw when step is not time-based using mockk`() {
        val step = mockk<SingleStep> {
            every { length } returns StepLength(1000, StepLength.LengthUnit.METERS)
            every { target } returns StepTarget(50, 100)
            every { ramp } returns true
        }

        assertThrows<IllegalStateException> {
            RampConverter(step).toRampToMultiStep()
        }
    }
}
