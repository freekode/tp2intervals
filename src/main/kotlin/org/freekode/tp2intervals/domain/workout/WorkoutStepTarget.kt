package org.freekode.tp2intervals.domain.workout

data class WorkoutStepTarget(
    val unit: TargetUnit,
    val min: Int,
    val max: Int
) {
    enum class TargetUnit {
        FTP_PERCENTAGE,
        LTHR_PERCENTAGE,
        RPM,
    }
}
