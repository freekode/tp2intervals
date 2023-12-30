package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
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
            WorkoutExternalData(tpWorkout.workoutId, null, workoutContent)
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
            WorkoutExternalData(tpNote.id.toString(), null, null)
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
