package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class WorkoutStepToThirdPartyStructureMapper(
    private val objectMapper: ObjectMapper,
) {

    fun mapToWorkoutStructureStr(workout: Workout): String? {
        if (workout.steps.isEmpty()) {
            return null
        }
        val structure = mapToWorkoutStructure(workout.steps)
        return objectMapper.copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(structure)
    }

    private fun mapToWorkoutStructure(steps: List<WorkoutStep>): ThirdPartyWorkoutStructureDTO {
        val stepDTOs = steps.map { mapToStructureStep(it) }

        return ThirdPartyWorkoutStructureDTO(
            stepDTOs,
            ThirdPartyWorkoutStructureDTO.LengthMetric.duration,
            ThirdPartyWorkoutStructureDTO.IntensityMetric.percentOfFtp,
            ThirdPartyWorkoutStructureDTO.IntensityTargetOrRange.range,
            null
        )
    }

    private fun mapToStructureStep(workoutStep: WorkoutStep): StructureStepDTO {
        if (workoutStep.isSingleStep()) {
            val singleStepDTO = mapToStepDTO(workoutStep)
            return StructureStepDTO.singleStep(singleStepDTO)
        } else {
            val stepDTOs = workoutStep.steps.map { mapToStepDTO(it) }
            return StructureStepDTO.multiStep(workoutStep.repetitions, stepDTOs)
        }
    }

    private fun mapToStepDTO(workoutStep: WorkoutStep) =
        StepDTO(
            workoutStep.title,
            LengthDTO.seconds(workoutStep.duration.seconds),
            workoutStep.targets.map { mapToTargetDTO(it) },
            StepDTO.IntensityClass.findByIntensityType(workoutStep.intensity),
            null
        )

    private fun mapToTargetDTO(workoutStepTarget: WorkoutStepTarget) =
        when (workoutStepTarget.unit) {
            WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE -> TargetDTO.powerTarget(
                workoutStepTarget.min, workoutStepTarget.max,
            )

            WorkoutStepTarget.TargetUnit.RPM -> TargetDTO.cadenceTarget(
                workoutStepTarget.min, workoutStepTarget.max,
            )

            else -> throw RuntimeException("i cant do it yet")
        }

}
