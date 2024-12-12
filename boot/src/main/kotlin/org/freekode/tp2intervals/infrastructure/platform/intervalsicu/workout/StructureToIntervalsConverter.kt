package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.structure.*
import org.freekode.tp2intervals.domain.workout.structure.StepLength.LengthUnit

class StructureToIntervalsConverter(
    private val structure: WorkoutStructure,
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
        val length = toStepLength(workoutStep.length)
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

        return "- $name $length $target$targetUnitStr ${structure.modifier.value} $cadence"
    }

    private fun toStepLength(length: StepLength): String {
        return when (length.unit) {
            LengthUnit.SECONDS -> length.value.toString().substring(2).lowercase()
            LengthUnit.METERS -> length.value.toString().substring(2).lowercase()
        }
    }
}
