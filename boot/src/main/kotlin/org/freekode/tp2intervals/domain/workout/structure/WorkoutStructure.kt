package org.freekode.tp2intervals.domain.workout.structure

data class WorkoutStructure(
    val target: TargetUnit,
    val steps: List<WorkoutStep>,
) {
    init {
        if (steps.isEmpty()) throw RuntimeException("There must be at least one step in workout structure")
    }

    enum class TargetUnit {
        FTP_PERCENTAGE,
        LTHR_PERCENTAGE,
        PACE_PERCENTAGE,
    }
}
