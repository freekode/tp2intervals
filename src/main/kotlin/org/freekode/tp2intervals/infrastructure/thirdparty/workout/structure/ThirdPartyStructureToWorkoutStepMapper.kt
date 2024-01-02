package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import java.time.Duration

class ThirdPartyStructureToWorkoutStepMapper(
    private val thirdPartyWorkoutStructureDTO: ThirdPartyWorkoutStructureDTO
) {

    fun mapToWorkoutSteps(): List<WorkoutStep> {
        return thirdPartyWorkoutStructureDTO.structure.map {
            if (it.length.isSingle()) {
                mapSingleStep(it)
            } else {
                mapMultiStep(it)
            }
        }
    }

    private fun mapSingleStep(structureStepDTO: StructureStepDTO): WorkoutStep {
        val stepDTO = structureStepDTO.steps[0]
        return WorkoutStep(
            stepDTO.name,
            1,
            stepDTO.length.mapDuration(),
            stepDTO.targets.map { mapTarget(it) },
            stepDTO.intensityClass?.type ?: StepIntensityType.ACTIVE,
            listOf()
        )
    }

    private fun mapMultiStep(it: StructureStepDTO): WorkoutStep {
        return WorkoutStep("1", 1, Duration.ZERO, listOf(), StepIntensityType.REST, listOf())
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
