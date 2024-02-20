package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class StructureToTPConverter(
    private val objectMapper: ObjectMapper,
    private val structure: WorkoutStructure,
) {

    fun toTPStructureStr(): String {
        val structure = mapToWorkoutStructure(structure.steps)
        return objectMapper.copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(structure)
    }

    private fun mapToWorkoutStructure(steps: List<WorkoutStep>): TPWorkoutStructureDTO {
        val stepDTOs = steps.map { mapToStructureStep(it) }

        return TPWorkoutStructureDTO(
            stepDTOs,
            TPWorkoutStructureDTO.LengthMetric.duration,
            TPTargetMapper.getByTargetUnit(structure.target),
            TPWorkoutStructureDTO.IntensityTargetOrRange.range,
            null
        )
    }

    private fun mapToStructureStep(workoutStep: WorkoutStep): TPStructureStepDTO {
        return if (workoutStep.isSingleStep()) {
            val singleStep = workoutStep as WorkoutSingleStep
            if (singleStep.ramp) {
                mapMultiStep(workoutStep.convertRampToMultiStep())
            } else {
                mapSingleStep(singleStep)
            }
        } else {
            mapMultiStep(workoutStep as WorkoutMultiStep)
        }
    }

    private fun mapSingleStep(singleStep: WorkoutSingleStep): TPStructureStepDTO {
        val singleStepDTO = mapToStepDTO(singleStep)
        return TPStructureStepDTO.singleStep(singleStepDTO)
    }

    private fun mapMultiStep(workoutStep: WorkoutMultiStep): TPStructureStepDTO {
        val stepDTOs = workoutStep.steps.map { mapToStepDTO(it) }
        return TPStructureStepDTO.multiStep(workoutStep.repetitions, stepDTOs)
    }

    private fun mapToStepDTO(workoutStep: WorkoutSingleStep): TPStepDTO {
        val targetList = mutableListOf(TPTargetDTO.mainTarget(workoutStep.target.start, workoutStep.target.end))
        workoutStep.cadence
            ?.let { TPTargetDTO.cadenceTarget(it.start, workoutStep.cadence.end) }
            ?.also { targetList.add(it) }

        return TPStepDTO(
            workoutStep.title,
            TPLengthDTO.seconds(workoutStep.duration.seconds),
            targetList,
            TPStepDTO.IntensityClass.active,
            null
        )
    }
}
