package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class ThirdPartyStructureToWorkoutStepMapper(
    private val thirdPartyWorkoutStructureDTO: ThirdPartyWorkoutStructureDTO
) {

    fun mapToWorkoutSteps(): List<WorkoutStep> {
        return thirdPartyWorkoutStructureDTO.structure.map {
            when (it.type) {
                StructureStepDTO.StructureType.step -> mapSingleStep(it.steps[0])
                StructureStepDTO.StructureType.repetition -> mapMultiStep(it)
                StructureStepDTO.StructureType.rampUp -> mapMultiStep(it)
            }
        }
    }

    private fun mapSingleStep(stepDTO: StepDTO): WorkoutSingleStep {
        return WorkoutSingleStep(
            stepDTO.name,
            stepDTO.length.mapDuration(),
            getMainTarget(stepDTO.targets),
            getSecondaryTarget(stepDTO.targets),
            stepDTO.intensityClass?.type ?: StepIntensityType.ACTIVE,
        )
    }

    private fun mapMultiStep(structureStepDTO: StructureStepDTO): WorkoutStep {
        return WorkoutMultiStep.multiStep(
            "Reps",
            structureStepDTO.length.reps().toInt(),
            structureStepDTO.steps.map { mapSingleStep(it) },
        )
    }

    private fun getMainTarget(targets: List<TargetDTO>): WorkoutStepTarget {
        val target = targets.first { it.unit == null }
        return WorkoutStepTarget(
            thirdPartyWorkoutStructureDTO.primaryIntensityMetric.targetUnit,
            target.minValue,
            target.maxValue
        )
    }

    private fun getSecondaryTarget(targets: List<TargetDTO>): WorkoutStepTarget? {
        return targets
            .firstOrNull { it.unit != null }
            ?.let { WorkoutStepTarget(it.unit!!.targetUnit, it.minValue, it.maxValue) }
    }

}
