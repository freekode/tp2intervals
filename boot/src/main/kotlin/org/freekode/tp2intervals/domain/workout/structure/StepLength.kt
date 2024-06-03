package org.freekode.tp2intervals.domain.workout.structure

import java.io.Serializable
import java.time.Duration

data class StepLength(
    val value: Long,
    val type: LengthUnit,
) : Serializable {
    companion object {
        fun seconds(value: Long) = StepLength(value, LengthUnit.SECONDS)
        fun meters(value: Long) = StepLength(value, LengthUnit.METERS)
    }

    enum class LengthUnit {
        SECONDS,
        METERS,
    }

}
