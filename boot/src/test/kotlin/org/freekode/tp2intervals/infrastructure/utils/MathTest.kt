package org.freekode.tp2intervals.infrastructure.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MathTest {

    @Test
    fun `should calculate percentage difference for positive numbers`() {
        val result = Math.percentageDiff(110.0, 100.0)

        assertEquals(10.0, result)
    }

    @Test
    fun `should calculate percentage difference when second is larger`() {
        val result = Math.percentageDiff(90.0, 100.0)

        assertEquals(10.0, result)
    }

    @Test
    fun `should return zero when numbers are equal`() {
        val result = Math.percentageDiff(100.0, 100.0)

        assertEquals(0.0, result)
    }

    @Test
    fun `should handle zero as second value`() {
        val result = Math.percentageDiff(50.0, 0.0)

        assertEquals(Double.POSITIVE_INFINITY, result)
    }
}
