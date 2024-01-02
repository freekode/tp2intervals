package org.freekode.tp2intervals.domain.workout

data class WorkoutStepTarget(
    val type: TargetType,
    val unit: TargetUnit,
    val min: Int,
    val max: Int
) {
    enum class TargetType {
        POWER,
        CADENCE,
    }

    enum class TargetUnit {
        FTP_PERCENTAGE,
        RPM,
    }
}
