package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import java.time.Duration
import org.freekode.tp2intervals.infrastructure.PlatformException

class TPLengthDTO(
    var value: Long,
    var unit: org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.LengthUnit,
) {
    companion object {
        fun seconds(value: Long) =
            org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO(
                value,
                org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.LengthUnit.second
            )
        fun repetitions(value: Long) =
            org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO(
                value,
                org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.LengthUnit.repetition
            )
        fun single() =
            org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.Companion.repetitions(
                1
            )
    }

    fun reps(): Long {
        if (unit != org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.LengthUnit.repetition) {
            throw PlatformException("not a repetition length")
        }
        return value
    }

    fun mapDuration(): Duration {
        return when (unit) {
            org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPLengthDTO.LengthUnit.second -> Duration.ofSeconds(value)
            else -> throw PlatformException("i can't do that yet with $unit")
        }
    }

    enum class LengthUnit {
        step, second, repetition, meter
    }
}
