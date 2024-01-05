package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.springframework.stereotype.Component

@Component
class WorkoutStepToTPStructureMapper(
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

    private fun mapToWorkoutStructure(steps: List<WorkoutStep>): TPWorkoutStructureDTO {
        val stepDTOs = steps.map { mapToStructureStep(it) }

        return TPWorkoutStructureDTO(
            stepDTOs,
            TPWorkoutStructureDTO.LengthMetric.duration,
            TPWorkoutStructureDTO.IntensityMetric.percentOfFtp,
            TPWorkoutStructureDTO.IntensityTargetOrRange.range,
            null
        )
    }

    private fun mapToStructureStep(workoutStep: WorkoutStep): TPStructureStepDTO {
        if (workoutStep.isSingleStep()) {
            val singleStepDTO = mapToStepDTO(workoutStep as WorkoutSingleStep)
            return TPStructureStepDTO.singleStep(singleStepDTO)
        } else {
            val stepDTOs = (workoutStep as WorkoutMultiStep).steps.map { mapToStepDTO(it) }
            return TPStructureStepDTO.multiStep(workoutStep.repetitions, stepDTOs)
        }
    }

    private fun mapToStepDTO(workoutStep: WorkoutSingleStep) =
        TPStepDTO(
            workoutStep.title,
            TPLengthDTO.seconds(workoutStep.duration.seconds),
            listOf(mapToTargetDTO(workoutStep.target)),
            TPStepDTO.IntensityClass.findByIntensityType(workoutStep.intensity),
            null
        )

    private fun mapToTargetDTO(workoutStepTarget: WorkoutStepTarget) =
        when (workoutStepTarget.unit) {
            WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE -> TPTargetDTO.powerTarget(
                workoutStepTarget.min, workoutStepTarget.max,
            )

            WorkoutStepTarget.TargetUnit.RPM -> TPTargetDTO.cadenceTarget(
                workoutStepTarget.min, workoutStepTarget.max,
            )

            else -> throw RuntimeException("i cant do it yet")
        }

}
