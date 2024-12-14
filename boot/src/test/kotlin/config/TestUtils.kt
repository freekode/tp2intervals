package config

import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.junit.jupiter.api.Assertions.assertEquals

class TestUtils {
    companion object {
        fun assertStep(step: SingleStep, length: Long, lengthUnit: StepLength.LengthUnit, targetStart: Int, targetEnd: Int) {
            assertEquals(length, step.length.value)
            assertEquals(lengthUnit, step.length.unit)
            assertEquals(targetStart, step.target.start)
            assertEquals(targetEnd, step.target.end)
        }
    }
}
