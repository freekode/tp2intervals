package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class StructureToIntervalsConverter(
    private val structure: WorkoutStructure,
    private val additionalStepParam: String,
) {
    private val targetTypeMap = mapOf(
        WorkoutStructure.TargetUnit.FTP_PERCENTAGE to "%",
        WorkoutStructure.TargetUnit.LTHR_PERCENTAGE to "% LTHR",
        WorkoutStructure.TargetUnit.PACE_PERCENTAGE to "% Pace",
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
        val duration = workoutStep.duration.toString()
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

        return "- $name $duration $target$targetUnitStr $additionalStepParam $cadence"
    }
}
