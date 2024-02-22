package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import java.time.Duration
import org.freekode.tp2intervals.infrastructure.PlatformException

class TPLengthDTO(
    var value: Long,
    var unit: LengthUnit,
) {
    companion object {
        fun seconds(value: Long) = TPLengthDTO(value, LengthUnit.second)
        fun repetitions(value: Long) = TPLengthDTO(value, LengthUnit.repetition)
        fun single() = repetitions(1)
    }

    fun reps(): Long {
        if (unit != LengthUnit.repetition) {
            throw PlatformException("not a repetition length")
        }
        return value
    }

    fun mapDuration(): Duration {
        return when (unit) {
            LengthUnit.second -> Duration.ofSeconds(value)
            else -> throw PlatformException("i can't do that yet with $unit")
        }
    }

    enum class LengthUnit {
        step, second, repetition, meter
    }
}
