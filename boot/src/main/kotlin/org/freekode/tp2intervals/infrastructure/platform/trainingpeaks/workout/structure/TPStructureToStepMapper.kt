package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStepTarget

class TPStructureToStepMapper(
    private val tPWorkoutStructureDTO: TPWorkoutStructureDTO
) {

    fun mapToWorkoutSteps(): List<WorkoutStep> {
        return tPWorkoutStructureDTO.structure.map {
            when (it.type) {
                "step" -> mapSingleStep(it.steps.firstOrNull() ?: throw IllegalArgumentException("There is no step"))
                "repetition" -> mapMultiStep(it)
                "rampUp" -> mapMultiStep(it)
                "rampDown" -> mapMultiStep(it)
                else -> throw IllegalArgumentException("Unknown step type: ${it.type}")
            }
        }
    }

    private fun mapSingleStep(tPStepDTO: TPStepDTO): WorkoutSingleStep {
        return WorkoutSingleStep(
            tPStepDTO.name,
            tPStepDTO.length.mapDuration(),
            getMainTarget(tPStepDTO.targets),
            getSecondaryTarget(tPStepDTO.targets),
            false
        )
    }

    private fun mapMultiStep(TPStructureStepDTO: TPStructureStepDTO): WorkoutStep {
        return WorkoutMultiStep(
            null,
            TPStructureStepDTO.length!!.reps().toInt(),
            TPStructureStepDTO.steps.map { mapSingleStep(it) },
        )
    }

    private fun getMainTarget(targets: List<TPTargetDTO>): WorkoutStepTarget {
        val target = targets.first { it.unit == null }
        return WorkoutStepTarget(
            target.minValue ?: target.maxValue!!,
            target.maxValue ?: target.minValue!!,
        )
    }

    private fun getSecondaryTarget(targets: List<TPTargetDTO>): WorkoutStepTarget? {
        return targets
            .firstOrNull { it.unit != null }
            ?.let { WorkoutStepTarget(it.minValue!!, it.maxValue!!) }
    }

}
