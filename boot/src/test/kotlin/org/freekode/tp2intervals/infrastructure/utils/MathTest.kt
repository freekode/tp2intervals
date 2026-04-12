package org.freekode.tp2intervals.infrastructure.utils

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isZero

class MathTest {

    @Test
    fun `should calculate percentage difference for positive numbers`() {
        val result = Math.percentageDiff(110.0, 100.0)

        expectThat(result).isEqualTo(10.0)
    }

    @Test
    fun `should calculate percentage difference when second is larger`() {
        val result = Math.percentageDiff(90.0, 100.0)

        expectThat(result).isEqualTo(10.0)
    }

    @Test
    fun `should return zero when numbers are equal`() {
        val result = Math.percentageDiff(100.0, 100.0)

        expectThat(result).isZero()
    }

    @Test
    fun `should handle zero as second value`() {
        val result = Math.percentageDiff(50.0, 0.0)

        expectThat(result).isEqualTo(Double.POSITIVE_INFINITY)
    }
}
