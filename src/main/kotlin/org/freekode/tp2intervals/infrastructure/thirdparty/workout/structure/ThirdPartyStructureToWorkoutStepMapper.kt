package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class ThirdPartyStructureToWorkoutStepMapper(
    private val thirdPartyWorkoutStructureDTO: ThirdPartyWorkoutStructureDTO
) {

    fun mapToWorkoutSteps(): List<WorkoutStep> {
        return thirdPartyWorkoutStructureDTO.structure.map {
            if (it.length.isSingle()) {
                mapSingleStep(it.steps[0])
            } else {
                mapMultiStep(it)
            }
        }
    }

    private fun mapSingleStep(stepDTO: StepDTO): WorkoutStep {
        return WorkoutStep.singleStep(
            stepDTO.name,
            stepDTO.length.mapDuration(),
            stepDTO.targets.map { mapTarget(it) },
            stepDTO.intensityClass?.type ?: StepIntensityType.ACTIVE,
        )
    }

    private fun mapMultiStep(structureStepDTO: StructureStepDTO): WorkoutStep {
        return WorkoutStep.multiStep(
            "Reps",
            structureStepDTO.length.getReps().toInt(),
            structureStepDTO.steps.map { mapSingleStep(it) },
        )
    }

    private fun mapTarget(targetDTO: TargetDTO): WorkoutStepTarget {
        val unit = if (targetDTO.unit != null) {
            targetDTO.unit!!.targetUnit
        } else {
            thirdPartyWorkoutStructureDTO.primaryIntensityMetric.targetUnit
        }
        return WorkoutStepTarget(
            unit,
            targetDTO.minValue,
            targetDTO.maxValue
        )
    }

}
