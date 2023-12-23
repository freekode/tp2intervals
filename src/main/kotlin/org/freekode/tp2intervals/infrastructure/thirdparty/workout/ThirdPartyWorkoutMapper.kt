package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutStep
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
            tpWorkout.structure?.let { mapToWorkoutSteps(it) } ?: listOf(),
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

    private fun mapToWorkoutSteps(structure: ThirdPartyWorkoutStructureDTO): List<WorkoutStep> {
        val list = mutableListOf<WorkoutStep>()
        for (structureDTO in structure.structure) {
            when (structureDTO.type) {
                ThirdPartyWorkoutStructureDTO.StructureType.STEP -> {
                    val stepDTO = structureDTO.steps[0]
                }

                else -> throw RuntimeException("wrong")
            }
        }
        return listOf()
    }

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
