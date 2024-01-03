package org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.springframework.stereotype.Component

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
            val singleStepDTO = mapToStepDTO(workoutStep as WorkoutSingleStep)
            return StructureStepDTO.singleStep(singleStepDTO)
        } else {
            val stepDTOs = (workoutStep as WorkoutMultiStep).steps.map { mapToStepDTO(it) }
            return StructureStepDTO.multiStep(workoutStep.repetitions, stepDTOs)
        }
    }

    private fun mapToStepDTO(workoutStep: WorkoutSingleStep) =
        StepDTO(
            workoutStep.title,
            LengthDTO.seconds(workoutStep.duration.seconds),
            listOf(mapToTargetDTO(workoutStep.target)),
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
