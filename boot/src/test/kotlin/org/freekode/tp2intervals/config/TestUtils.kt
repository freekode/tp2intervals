package org.freekode.tp2intervals.config

import org.freekode.tp2intervals.domain.workout.structure.SingleStep
import org.freekode.tp2intervals.domain.workout.structure.StepLength
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.junit.jupiter.api.Assertions

class TestUtils {
    companion object {
        fun assertStep(step: WorkoutStep, length: Long, lengthUnit: StepLength.LengthUnit, targetStart: Int, targetEnd: Int) {
            val singleStep = step as SingleStep
            Assertions.assertEquals(length, singleStep.length.value)
            Assertions.assertEquals(lengthUnit, singleStep.length.unit)
            Assertions.assertEquals(targetStart, singleStep.target.start)
            Assertions.assertEquals(targetEnd, singleStep.target.end)
        }
    }
}