package org.freekode.tp2intervals.domain.workout

data class WorkoutStepTarget(
    val type: TargetType,
    val unit: TargetUnit,
    val value: TargetValue,
) {
    enum class TargetType {
        POWER,
        CADENCE
    }

    enum class TargetUnit {
        FTP_PERCENTAGE,
        CADENCE,
        UNKNOWN
    }

    data class TargetValue(
        val min: Int,
        val max: Int
    ) {
        constructor(value: Int) : this(value, value)

        fun isSingleValue() = min == max
    }
}
