package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.infrastructure.thirdparty.ThirdPartyApiClient
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.structure.ThirdPartyStructureToWorkoutStepMapper
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ThirdPartyWorkoutMapper(
    private val thirdPartyApiClient: ThirdPartyApiClient,
) {
    fun mapToWorkout(tpWorkout: ThirdPartyWorkoutDTO): Workout {
        val steps = tpWorkout.structure
            ?.let { ThirdPartyStructureToWorkoutStepMapper(it).mapToWorkoutSteps() }
            ?: listOf()
        val workoutContent = mapWorkoutContent(tpWorkout)

        return Workout(
            tpWorkout.workoutDay.toLocalDate(),
            tpWorkout.getWorkoutType()!!.trainingType,
            tpWorkout.title.ifBlank { "Workout" },
            tpWorkout.description,
            tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
            tpWorkout.tssPlanned,
            steps,
            WorkoutExternalData.thirdParty(tpWorkout.workoutId, workoutContent)
        )
    }

    fun mapToWorkout(tpNote: ThirdPartyNoteDTO): Workout {
        return Workout.note(
            tpNote.noteDate.toLocalDate(),
            tpNote.title,
            tpNote.description,
            WorkoutExternalData.thirdParty(tpNote.id.toString(), null)
        )
    }

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
