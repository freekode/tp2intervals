package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class StructureToIntervalsConverter(
    private val structure: WorkoutStructure
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
        val targetUnitStr = targetTypeMap[structure.target]
            ?: throw IllegalArgumentException("cant find target unit ${structure.target}")
        val title: String = workoutStep.title
        val duration: Duration = workoutStep.duration
        val min: Int = workoutStep.target.start
        val max: Int = workoutStep.target.end
        val rpmMin: Int? = workoutStep.cadence?.start
        val rpmMax: Int? = workoutStep.cadence?.end

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
