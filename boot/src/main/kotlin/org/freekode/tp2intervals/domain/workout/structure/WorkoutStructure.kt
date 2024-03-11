package org.freekode.tp2intervals.domain.workout.structure

import java.io.Serializable

data class WorkoutStructure(
    val target: TargetUnit,
    val steps: List<WorkoutStep>,
) : Serializable {
    init {
        if (steps.isEmpty()) throw IllegalStateException("There must be at least one step in workout structure")
    }

    enum class TargetUnit {
        FTP_PERCENTAGE,
        LTHR_PERCENTAGE,
        PACE_PERCENTAGE,
    }
}
