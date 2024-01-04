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
    enum class TargetType(val targetType: WorkoutStepTarget.TargetType, val strType: String) {
        POWER(WorkoutStepTarget.TargetType.POWER, "%"),
        HR(WorkoutStepTarget.TargetType.HR, "% HR"),
        LTHR(WorkoutStepTarget.TargetType.HR, "% LTHR");

        companion object {
            fun getByTargetType(targetType: WorkoutStepTarget.TargetType): TargetType =
                entries.first { it.targetType == targetType }
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
