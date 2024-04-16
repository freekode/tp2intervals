package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import java.time.Duration
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.infrastructure.PlatformException

class TPLengthDTO(
    var value: Long,
    var unit: String,
) {
    companion object {
        fun seconds(value: Long) = TPLengthDTO(value, "second")
        fun repetitions(value: Long) = TPLengthDTO(value, "repetition")
        fun single() = repetitions(1)
    }

    fun reps(): Long {
        if (unit != "repetition") {
            throw IllegalArgumentException("not a repetition length")
        }
        return value
    }

    fun mapDuration(): Duration {
        return when (unit) {
            "second" -> Duration.ofSeconds(value)
            else -> throw IllegalArgumentException("Can't map such unit - $unit")
        }
    }
}
