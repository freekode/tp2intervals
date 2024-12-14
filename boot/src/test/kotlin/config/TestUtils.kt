package config

import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.junit.jupiter.api.Assertions.assertEquals

class TestUtils {
    companion object {
        fun assertStep(step: WorkoutStep, length: Long, lengthUnit: StepLength.LengthUnit, targetStart: Int, targetEnd: Int) {
            val singleStep = step as SingleStep
            assertEquals(length, singleStep.length.value)
            assertEquals(lengthUnit, singleStep.length.unit)
            assertEquals(targetStart, singleStep.target.start)
            assertEquals(targetEnd, singleStep.target.end)
        }
    }
}
