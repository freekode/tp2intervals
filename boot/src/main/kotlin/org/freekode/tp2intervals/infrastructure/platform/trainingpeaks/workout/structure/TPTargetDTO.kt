package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class TPTargetDTO(
    var minValue: Int,
    var maxValue: Int,
    var unit: TargetUnitDTO?,
) {
    companion object {
        fun powerTarget(minValue: Int, maxValue: Int): TPTargetDTO = TPTargetDTO(minValue, maxValue, null)

        fun cadenceTarget(minValue: Int, maxValue: Int): TPTargetDTO =
            TPTargetDTO(minValue, maxValue, TargetUnitDTO.roundOrStridePerMinute)
    }

    enum class TargetUnitDTO(
        val targetUnit: WorkoutStepTarget.TargetUnit,
    ) {
        roundOrStridePerMinute(WorkoutStepTarget.TargetUnit.RPM)
    }
}
