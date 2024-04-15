package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

class TPTargetDTO(
    var minValue: Int?,
    var maxValue: Int?,
    var unit: String?,
) {
    companion object {
        fun mainTarget(value: Int): TPTargetDTO = TPTargetDTO(value, null, null)

        fun mainTarget(minValue: Int, maxValue: Int): TPTargetDTO = TPTargetDTO(minValue, maxValue, null)

        fun cadenceTarget(value: Int): TPTargetDTO =
            TPTargetDTO(value, null, "roundOrStridePerMinute")

        fun cadenceTarget(minValue: Int, maxValue: Int): TPTargetDTO =
            TPTargetDTO(minValue, maxValue, "roundOrStridePerMinute")
    }
}
