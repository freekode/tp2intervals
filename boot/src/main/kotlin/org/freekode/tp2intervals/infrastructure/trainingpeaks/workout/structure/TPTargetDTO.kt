package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure

class TPTargetDTO(
    var minValue: Int,
    var maxValue: Int,
    var unit: String?,
) {
    companion object {
        fun mainTarget(minValue: Int, maxValue: Int): TPTargetDTO = TPTargetDTO(minValue, maxValue, null)

        fun cadenceTarget(minValue: Int, maxValue: Int): TPTargetDTO =
            TPTargetDTO(minValue, maxValue, "roundOrStridePerMinute")
    }
}
