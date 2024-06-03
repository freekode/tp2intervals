package org.freekode.tp2intervals.domain.workout.structure

import java.io.Serializable

data class StepTarget(
    val start: Int,
    val end: Int
): Serializable {
    fun isSingleValue(): Boolean = start == end
}
