package org.freekode.tp2intervals.domain.workout.structure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StepTargetTest {

    @Test
    fun `should return true when start equals end`() {
        val target = StepTarget(75, 75)

        assert(target.isSingleValue())
    }

    @Test
    fun `should return false when start differs from end`() {
        val target = StepTarget(50, 100)

        assert(!target.isSingleValue())
    }

    @Test
    fun `should handle negative values`() {
        val target = StepTarget(-10, 10)

        assert(!target.isSingleValue())
    }
}
