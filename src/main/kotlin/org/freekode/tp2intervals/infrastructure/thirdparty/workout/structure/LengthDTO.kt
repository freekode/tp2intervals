package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import java.time.Duration

class LengthDTO(
    var value: Long,
    var unit: LengthUnit,
) {
    companion object {
        fun seconds(value: Long) = LengthDTO(value, LengthUnit.second)
        fun repetitions(value: Long) = LengthDTO(value, LengthUnit.repetition)
    }

    fun isSingle() = value == 1L && unit == LengthUnit.repetition

    fun mapDuration(): Duration {
        if (unit != LengthUnit.second) {
            throw RuntimeException("i can't do that yet")
        }
        return Duration.ofSeconds(value)
    }

    enum class LengthUnit {
        step, second, repetition, meter
    }
}
