package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStepTarget
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class StructureToTPConverter(
    private val objectMapper: ObjectMapper,
    private val structure: WorkoutStructure,
) {
    companion object {
        fun toStructureString(objectMapper: ObjectMapper, workout: Workout): String? {
            return if (workout.structure != null) {
                StructureToTPConverter(objectMapper, workout.structure).toTPStructureStr()
            } else {
                null
            }
        }
    }

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
            "duration",
            TPTargetMapper.getByTargetUnit(structure.target),
            "range",
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
        val mainTarget = toMainTarget(workoutStep.target)
        val cadenceTarget = workoutStep.cadence?.let { toCadenceTarget(workoutStep.cadence) }
        val targetList = mutableListOf(mainTarget, cadenceTarget).filterNotNull()

        return TPStepDTO(
            workoutStep.name,
            TPLengthDTO.seconds(workoutStep.duration.seconds),
            targetList,
            "active",
            null
        )
    }

    private fun toMainTarget(target: WorkoutStepTarget) =
        if (target.isSingleValue()) {
            TPTargetDTO.mainTarget(target.start)
        } else {
            TPTargetDTO.mainTarget(target.start, target.end)
        }

    private fun toCadenceTarget(target: WorkoutStepTarget) =
        if (target.isSingleValue()) {
            TPTargetDTO.cadenceTarget(target.start)
        } else {
            TPTargetDTO.cadenceTarget(target.start, target.end)
        }

}
