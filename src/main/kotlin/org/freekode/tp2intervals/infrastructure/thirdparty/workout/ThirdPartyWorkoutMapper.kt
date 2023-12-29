package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.freekode.tp2intervals.domain.workout.WorkoutType
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyApiClient
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ThirdPartyWorkoutMapper(
    private val thirdPartyApiClient: ThirdPartyApiClient,
) {
    fun mapToWorkout(tpWorkout: ThirdPartyWorkoutDTO): Workout {
        val workoutContent = mapWorkoutContent(tpWorkout)
        return Workout(
            tpWorkout.workoutDay.toLocalDate(),
            tpWorkout.getWorkoutType()!!.workoutType,
            tpWorkout.title.ifBlank { "Workout" },
            tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
            tpWorkout.tssPlanned,
            tpWorkout.description,
            listOf(),
            workoutContent
        )
    }

    fun mapToWorkout(tpNote: ThirdPartyNoteDTO): Workout {
        return Workout(
            tpNote.noteDate.toLocalDate(),
            WorkoutType.NOTE,
            tpNote.title,
            null,
            null,
            tpNote.description,
            listOf(),
            null,
        )
    }

    fun mapToWorkoutStructure(workout: Workout): ThirdPartyWorkoutStructureDTO {
        val stepDTOs = listOf(mapSingleStep(workout.steps[0]))

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
            null, null
        )
        return ThirdPartyWorkoutStructureDTO.StructureDTO(
            ThirdPartyWorkoutStructureDTO.StructureType.step,
            ThirdPartyWorkoutStructureDTO.LengthDTO(1, ThirdPartyWorkoutStructureDTO.LengthUnit.repetition),
            listOf(stepDTO)
        )
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
        val userId = getUserId()
        return thirdPartyApiClient.downloadWorkoutZwo(userId, tpWorkoutId)
    }

    private fun getUserId(): String = thirdPartyApiClient.getUser().userId!!
}
