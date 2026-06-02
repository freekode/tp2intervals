package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.structure.*
import org.freekode.tp2intervals.domain.workout.structure.StepLength.LengthUnit
import java.time.Duration

class ToIntervalsStructureConverter(
    private val structure: WorkoutStructure,
) {

    private var cadenceWasReset: Boolean = true

    private val targetTypeMap = mapOf(
        WorkoutStructure.TargetUnit.FTP_PERCENTAGE to "%",
        WorkoutStructure.TargetUnit.LTHR_PERCENTAGE to "% LTHR",
        WorkoutStructure.TargetUnit.PACE_PERCENTAGE to "% Pace",
        WorkoutStructure.TargetUnit.RELATIVE_PERCEIVED_EFFORT to "",
    )

    fun toIntervalsStructureStr(): String {
        cadenceWasReset = true
        return structure.steps.joinToString(separator = "\n") { toIntervalsStep(it) }
    }

    private fun toIntervalsStep(workoutStep: WorkoutStep): String {
        return if (workoutStep.isSingleStep()) {
            getStepString(workoutStep as SingleStep)
        } else {
            "\n" + mapMultiStep(workoutStep as MultiStep) + "\n"
        }
    }

    private fun mapMultiStep(multiStep: MultiStep): String {
        val steps = listOf("${multiStep.repetitions}x") + multiStep.steps.map { toIntervalsStep(it) }
        return steps.joinToString(separator = "\n")
    }

    private fun getStepString(workoutStep: SingleStep): String {
        val description = getDescription(structure.target, workoutStep)
        val notes = getNotes(workoutStep)
        val length = toStepLength(workoutStep.length)
        val targetUnitStr = targetTypeMap[structure.target]!!
        val target = toTarget(structure.target, workoutStep.target)
        val cadence = toCadence(workoutStep)

        val stepLine = "- $description $length $target$targetUnitStr ${structure.modifier.value} $cadence".trim()

        return "$stepLine$notes"
    }

    private fun getDescription(targetUnit: WorkoutStructure.TargetUnit, workoutStep: SingleStep) : String {
        var description = workoutStep.name.orEmpty().replace("\\", "/")
//            ?.takeIf { it.isNotEmpty() }
//            ?.let { "<b>$it</b>" } ?: "" //TODO Format Description

        if (targetUnit == WorkoutStructure.TargetUnit.RELATIVE_PERCEIVED_EFFORT) {
            if (workoutStep.target.isSingleValue()) {
                description += " RPE ${workoutStep.target.start}"
            } else {
                description += " RPE ${workoutStep.target.start}-${workoutStep.target.end}"
            }
        }

        return description.trim()
    }

    private fun getNotes(workoutStep: SingleStep): String {
        return workoutStep.notes?.replace("\\", "/")
            ?.takeIf { it.isNotEmpty() }
            ?.let { "  \nNotes: $it" } ?: ""
//            ?.let { "  \n<i>Notes: $it</i>" } ?: "" //TODO Format Description
    }

    private fun toCadence(workoutStep: SingleStep): String {
        if (workoutStep.cadence != null) {
            val it = workoutStep.cadence
            cadenceWasReset = false
            return if (it.isSingleValue()) " ${it.start}rpm" else " ${it.start}-${it.end}rpm"
        }

        if (!cadenceWasReset) {
            cadenceWasReset = true
            return " 0rpm"
        }

        return ""
    }

    private fun toStepLength(length: StepLength) = when (length.unit) {
        LengthUnit.SECONDS -> Duration.ofSeconds(length.value).toString().substring(2).lowercase()
        LengthUnit.METERS -> (length.value / 1000.0).toString() + "km"
    }

    private fun toTarget(targetUnit: WorkoutStructure.TargetUnit, target: StepTarget) : String {
        val targetVal =
            if (targetUnit == WorkoutStructure.TargetUnit.RELATIVE_PERCEIVED_EFFORT) {
                ""
            } else {
                if (target.isSingleValue()) {
                    "${target.start}"
                } else {
                    "${target.start}-${target.end}"
                }
            }

        return targetVal
    }
}
