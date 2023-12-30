package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyApiClient
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ThirdPartyWorkoutMapper(
    private val thirdPartyApiClient: ThirdPartyApiClient,
    private val objectMapper: ObjectMapper,
) {
    fun mapToWorkout(tpWorkout: ThirdPartyWorkoutDTO): Workout {
        val workoutContent = mapWorkoutContent(tpWorkout)
        return Workout(
            tpWorkout.workoutDay.toLocalDate(),
            tpWorkout.getWorkoutType()!!.trainingType,
            tpWorkout.title.ifBlank { "Workout" },
            tpWorkout.description,
            tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
            tpWorkout.tssPlanned,
            listOf(),
            workoutContent
        )
    }

    fun mapToWorkout(tpNote: ThirdPartyNoteDTO): Workout {
        return Workout(
            tpNote.noteDate.toLocalDate(),
            TrainingType.NOTE,
            tpNote.title,
            tpNote.description,
            null,
            null,
            listOf(),
            null,
        )
    }

    fun mapToWorkoutStructureStr(workout: Workout): String? {
        if (workout.steps.isEmpty()) {
            return null
        }
        val structure = mapToWorkoutStructure(workout)
        return objectMapper.copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(structure)
    }

    fun mapToWorkoutStructure(workout: Workout): ThirdPartyWorkoutStructureDTO {
        val stepDTOs = workout.steps.map { if (it.isSingleStep()) mapSingleStep(it) else mapMultiStep(it) }

        return ThirdPartyWorkoutStructureDTO(
            stepDTOs,
            "duration",
            "percentOfFtp",
            "range",
        )
    }

    private fun mapSingleStep(workoutStep: WorkoutStep): ThirdPartyWorkoutStructureDTO.StructureDTO {
        val stepDTO = ThirdPartyWorkoutStructureDTO.StepDTO(
            "Step",
            ThirdPartyWorkoutStructureDTO.LengthDTO(
                workoutStep.duration.seconds.toInt(),
                ThirdPartyWorkoutStructureDTO.LengthUnit.second
            ),
            workoutStep.targets.map { mapToTargetDTO(it) },
            ThirdPartyWorkoutStructureDTO.IntensityClass.findByIntensityType(workoutStep.intensity),
            null
        )
        return ThirdPartyWorkoutStructureDTO.StructureDTO(
            ThirdPartyWorkoutStructureDTO.StructureType.step,
            ThirdPartyWorkoutStructureDTO.LengthDTO(1, ThirdPartyWorkoutStructureDTO.LengthUnit.repetition),
            listOf(stepDTO)
        )
    }

    private fun mapMultiStep(workoutStep: WorkoutStep): ThirdPartyWorkoutStructureDTO.StructureDTO {
        throw RuntimeException("i can't")
    }

    private fun mapToTargetDTO(workoutStepTarget: WorkoutStepTarget) =
        ThirdPartyWorkoutStructureDTO.TargetDTO(workoutStepTarget.value.min, workoutStepTarget.value.max, null)

    private fun mapWorkoutContent(tpWorkout: ThirdPartyWorkoutDTO) =
        if (tpWorkout.structure != null) {
            downloadWorkoutContent(tpWorkout.workoutId)
        } else {
            null
        }

    private fun downloadWorkoutContent(tpWorkoutId: String): String {
        val userId = thirdPartyApiClient.getUser().userId!!
        return thirdPartyApiClient.downloadWorkoutZwo(userId, tpWorkoutId)
    }
}
