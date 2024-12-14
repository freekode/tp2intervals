package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import org.freekode.tp2intervals.domain.workout.structure.*

class ConverterFromTPStructure(
    private val structureDTO: TPWorkoutStructureDTO
) {
    companion object {
        fun toWorkoutStructure(structure: TPWorkoutStructureDTO): WorkoutStructure {
            val steps = ConverterFromTPStructure(structure).mapToWorkoutSteps()
            return WorkoutStructure(structure.toTargetUnit(), steps)
        }
    }

    fun mapToWorkoutSteps(): List<WorkoutStep> {
        return structureDTO.structure.map {
            when (it.type) {
                "step" -> mapSingleStep(it.steps.firstOrNull() ?: throw IllegalArgumentException("There is no step"))
                "repetition" -> mapMultiStep(it)
                "rampUp" -> mapMultiStep(it)
                "rampDown" -> mapMultiStep(it)
                else -> throw IllegalArgumentException("Unknown step type: ${it.type}")
            }
        }
    }

    private fun mapSingleStep(tPStepDTO: TPStepDTO): SingleStep {
        return SingleStep(
            tPStepDTO.name,
            tPStepDTO.length!!.toStepLength(),
            tPStepDTO.toMainTarget(),
            tPStepDTO.toSecondaryTarget(),
            false
        )
    }

    private fun mapMultiStep(tPStructureStepDTO: TPStructureStepDTO): WorkoutStep {
        return MultiStep(
            null,
            tPStructureStepDTO.length!!.reps().toInt(),
            tPStructureStepDTO.steps.map { mapSingleStep(it) },
        )
    }
}
