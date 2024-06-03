package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.StepStructure

class StructureToIntervalsConverter(
    private val structure: StepStructure
) {
    private val targetTypeMap = mapOf(
        StepStructure.TargetUnit.FTP_PERCENTAGE to "%",
        StepStructure.TargetUnit.LTHR_PERCENTAGE to "% LTHR",
        StepStructure.TargetUnit.PACE_PERCENTAGE to "% Pace",
    )

    fun toIntervalsStructureStr(): String {
        return structure.steps.joinToString(separator = "\n") { toIntervalsStep(it) }
    }

    private fun toIntervalsStep(workoutStep: WorkoutStep): String {
        return if (workoutStep.isSingleStep()) {
            getStepString(workoutStep as WorkoutSingleStep)
        } else {
            "\n" + mapMultiStep(workoutStep as WorkoutMultiStep) + "\n"
        }
    }

    private fun mapMultiStep(workoutMultiStep: WorkoutMultiStep): String {
        val steps = listOf("${workoutMultiStep.repetitions}x") + workoutMultiStep.steps.map { toIntervalsStep(it) }
        return steps.joinToString(separator = "\n")
    }

    private fun getStepString(workoutStep: WorkoutSingleStep): String {
        val name = workoutStep.name.orEmpty().replace("\\", "/")
        val duration = workoutStep.length.value.toString()
            .substring(2)
            .lowercase()
        val targetUnitStr = targetTypeMap[structure.target]!!
        val target: String = if (workoutStep.target.isSingleValue()) {
            "${workoutStep.target.start}"
        } else {
            "${workoutStep.target.start}-${workoutStep.target.end}"
        }
        val cadence = workoutStep.cadence?.let {
            if (it.isSingleValue()) {
                "${it.start}rpm"
            } else {
                "${it.start}-${it.end}rpm"
            }
        } ?: ""

        return "- $name $duration $target$targetUnitStr $cadence"
    }
}
