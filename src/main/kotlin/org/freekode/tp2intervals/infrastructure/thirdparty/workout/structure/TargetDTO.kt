package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class TargetDTO(
    var minValue: Int,
    var maxValue: Int,
    var unit: TargetUnit?,
) {
    companion object {
        fun powerTarget(minValue: Int, maxValue: Int): TargetDTO = TargetDTO(minValue, maxValue, null)

        fun cadenceTarget(minValue: Int, maxValue: Int): TargetDTO =
            TargetDTO(minValue, maxValue, TargetUnit.roundOrStridePerMinute)
    }

    fun mapTargetType(): WorkoutStepTarget.TargetType {
        return when (unit) {
            null -> WorkoutStepTarget.TargetType.POWER
            TargetUnit.roundOrStridePerMinute -> WorkoutStepTarget.TargetType.CADENCE
        }
    }

    enum class TargetUnit {
        roundOrStridePerMinute
    }
}
