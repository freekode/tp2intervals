package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import java.time.Duration

data class IntervalsWorkoutStep(
    val title: String,
    val duration: Duration,
    val min: String,
    val max: String,
    val targetType: TargetType,
    val rpmMin: Int?,
    val rpmMax: Int?,
) {
    enum class TargetType(val targetUnit: WorkoutStepTarget.TargetUnit, val strType: String) {
        POWER(WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE, "%"),
        HR(WorkoutStepTarget.TargetUnit.MAX_HR_PERCENTAGE, "% HR"),
        LTHR(WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE, "% LTHR"),
        PACE(WorkoutStepTarget.TargetUnit.PACE_PERCENTAGE, "% Pace");

        companion object {
            fun findByTargetUnit(targetUnit: WorkoutStepTarget.TargetUnit): TargetType =
                entries.firstOrNull { it.targetUnit == targetUnit }.takeIf { it != null }
                    ?: throw IllegalArgumentException("cant much target unit $targetUnit")
        }
    }

    fun getStepString(): String {
        val durationStr = duration.toString()
            .substring(2)
            .lowercase()
        val rpmStr = if (rpmMin != null && rpmMax != null) {
            "$rpmMin-${rpmMax}rpm"
        } else {
            ""
        }

        return "- '$title' $durationStr $min-$max${targetType.strType} $rpmStr"
    }
}
