package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.infrastructure.utils.Base64
import org.freekode.tp2intervals.infrastructure.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure.TPStructureToWorkoutStepMapper
import org.springframework.stereotype.Component

@Component
class TPWorkoutMapper(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
) {
    fun mapToWorkout(tpWorkout: TPWorkoutDTO): Workout {
        val steps = tpWorkout.structure
            ?.let { TPStructureToWorkoutStepMapper(it).mapToWorkoutSteps() }
            ?: listOf()

        var description = tpWorkout.description.orEmpty()
        description += tpWorkout.coachComments?.let { "\n- - - -\n$it" }.orEmpty()

        val workoutContent = getWorkoutContent(tpWorkout)

        return Workout(
            tpWorkout.workoutDay.toLocalDate(),
            tpWorkout.getWorkoutType()!!.trainingType,
            tpWorkout.title.ifBlank { "Workout" },
            description,
            tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
            tpWorkout.tssPlanned,
            steps,
            WorkoutExternalData.thirdParty(tpWorkout.workoutId, workoutContent)
        )
    }

    fun mapToWorkout(tpNote: TPNoteDTO): Workout {
        return Workout.note(
            tpNote.noteDate.toLocalDate(),
            tpNote.title,
            tpNote.description,
            WorkoutExternalData.thirdParty(tpNote.id.toString(), null)
        )
    }

    private fun getWorkoutContent(tpWorkout: TPWorkoutDTO): String? {
        if (tpWorkout.structure == null) {
            return null
        }

        val userId = trainingPeaksApiClient.getUser().userId!!
        val resource = trainingPeaksApiClient.downloadWorkoutFit(userId, tpWorkout.workoutId)
        return Base64.toString(resource)
    }

}
