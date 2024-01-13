package org.freekode.tp2intervals.domain.workout

data class WorkoutStepTarget(
    val unit: TargetUnit,
    val start: Int,
    val end: Int
) {
    enum class TargetUnit {
        FTP_PERCENTAGE,
        MAX_HR_PERCENTAGE,
        LTHR_PERCENTAGE,
        PACE_PERCENTAGE,
        RPM,
        UNKNOWN
    }
}
