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
                TPStructureStepDTO.StructureType.step -> mapSingleStep(it.steps[0])
                TPStructureStepDTO.StructureType.repetition -> mapMultiStep(it)
                TPStructureStepDTO.StructureType.rampUp -> mapMultiStep(it)
                TPStructureStepDTO.StructureType.rampDown -> mapMultiStep(it)
            }
        }
    }

    private fun mapSingleStep(TPStepDTO: TPStepDTO): WorkoutSingleStep {
        return WorkoutSingleStep(
            TPStepDTO.name,
            TPStepDTO.length.mapDuration(),
            getMainTarget(TPStepDTO.targets),
            getSecondaryTarget(TPStepDTO.targets),
            false
        )
    }

    private fun mapMultiStep(TPStructureStepDTO: TPStructureStepDTO): WorkoutStep {
        return WorkoutMultiStep(
            "Reps",
            TPStructureStepDTO.length.reps().toInt(),
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
