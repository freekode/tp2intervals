package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPStructureToStepMapper
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO
import org.springframework.stereotype.Component

@Component
class TPToWorkoutConverter {
    fun toWorkout(tpWorkout: TPWorkoutResponseDTO): Workout {
        val workoutsStructure = if (tpWorkout.structure != null) toWorkoutStructure(tpWorkout.structure) else null
        var description = tpWorkout.description.orEmpty()
        description += tpWorkout.coachComments?.let { "\n- - - -\n$it" }.orEmpty()


        return Workout(
            tpWorkout.workoutDay.toLocalDate(),
            tpWorkout.getWorkoutType()!!,
            tpWorkout.title.ifBlank { "Workout" },
            description,
            tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
            tpWorkout.tssPlanned,
            workoutsStructure,
            getWorkoutExternalData(tpWorkout)
        )
    }

    private fun getWorkoutExternalData(tpWorkout: TPWorkoutResponseDTO): ExternalData {
        return ExternalData
            .empty()
            .withTrainingPeaks(tpWorkout.workoutId)
            .withSimpleString(tpWorkout.description ?: "")
    }

    fun toWorkout(tpNote: TPNoteResponseDTO): Workout {
        return Workout.note(
            tpNote.noteDate.toLocalDate(),
            tpNote.title,
            tpNote.description,
            ExternalData
                .empty()
                .withTrainingPeaks(tpNote.id.toString())
        )
    }

    private fun toWorkoutStructure(structure: TPWorkoutStructureDTO): WorkoutStructure {
        val steps = TPStructureToStepMapper(structure).mapToWorkoutSteps()
        return WorkoutStructure(
            structure.toTargetUnit(),
            steps
        )
    }
}
