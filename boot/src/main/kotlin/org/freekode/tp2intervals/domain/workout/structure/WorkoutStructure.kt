package org.freekode.tp2intervals.domain.workout.structure

import java.io.Serializable

data class WorkoutStructure(
    val target: TargetUnit,
    val steps: List<WorkoutStep>,
    val modifier: StepModifier = StepModifier.NONE
) : Serializable {
    init {
        if (steps.isEmpty()) throw IllegalStateException("There must be at least one step in workout structure")
    }

    fun addModifier(mod: StepModifier): WorkoutStructure {
        return WorkoutStructure(target, steps, mod)
    }

    enum class TargetUnit {
        FTP_PERCENTAGE,
        LTHR_PERCENTAGE,
        PACE_PERCENTAGE,
        RELATIVE_PERCEIVED_EFFORT,
    }
}
