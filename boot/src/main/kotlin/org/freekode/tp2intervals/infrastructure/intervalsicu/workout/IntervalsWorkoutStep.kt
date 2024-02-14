package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

data class IntervalsWorkoutStep(
    val title: String,
    val duration: Duration,
    val min: String,
    val max: String,
    val targetUnit: WorkoutStepTarget.TargetUnit,
    val rpmMin: Int?,
    val rpmMax: Int?,
) {
    private val targetUnitStr = targetTypeMap[targetUnit]
        ?: throw IllegalArgumentException("cant find target unit $targetUnit")

    companion object {
        private val targetTypeMap = mapOf(
            WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE to "%",
            WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE to "% LTHR",
            WorkoutStepTarget.TargetUnit.PACE_PERCENTAGE to "% Pace",
        )
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

        return "- ${title.replace("\\", "/")} $durationStr $min-$max${targetUnitStr} $rpmStr"
    }
}
