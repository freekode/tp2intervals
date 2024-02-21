package org.freekode.tp2intervals.infrastructure.trainingpeaks.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure.TPStructureToStepMapper
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.structure.TPWorkoutStructureDTO
import org.springframework.stereotype.Component

@Component
class TPToWorkoutConverter {
    fun toWorkout(tpWorkout: TPWorkoutDTO): Workout {
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
            WorkoutExternalData.trainingPeaks(tpWorkout.workoutId, null)
        )
    }

    fun toWorkout(tpNote: TPNoteDTO): Workout {
        return Workout.note(
            tpNote.noteDate.toLocalDate(),
            tpNote.title,
            tpNote.description,
            WorkoutExternalData.trainingPeaks(tpNote.id.toString(), null)
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
