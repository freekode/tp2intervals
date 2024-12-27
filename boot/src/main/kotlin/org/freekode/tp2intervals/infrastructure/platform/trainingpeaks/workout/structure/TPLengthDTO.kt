package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.StepLength

class TPLengthDTO(
    var value: Long,
    var unit: String,
) {
    companion object {
        fun fromStepLength(stepLength: StepLength): TPLengthDTO =
            when (stepLength.unit) {
                StepLength.LengthUnit.SECONDS -> TPLengthDTO(stepLength.value, "second")
                StepLength.LengthUnit.METERS -> TPLengthDTO(stepLength.value, "meter")
            }

        fun repetitions(value: Long) = TPLengthDTO(value, "repetition")
        fun single() = repetitions(1)
    }

    fun reps(): Long {
        if (unit != "repetition") {
            throw IllegalArgumentException("not a repetition length")
        }
        return value
    }

    fun toStepLength(): StepLength {
        return when (unit) {
            "second" -> StepLength.seconds(value)
            "meter" -> StepLength.meters(value)
            else -> throw IllegalArgumentException("Can't map such unit - $unit")
        }
    }
}
