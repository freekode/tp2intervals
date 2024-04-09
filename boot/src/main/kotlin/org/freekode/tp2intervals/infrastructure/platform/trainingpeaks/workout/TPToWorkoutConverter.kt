package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryItemDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.TPStructureToStepMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TPToWorkoutConverter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun toWorkout(tpWorkout: TPWorkoutResponseDTO): Workout {
        return toWorkout(tpWorkout, tpWorkout.workoutDay.toLocalDate())
    }

    fun toWorkout(tpWorkout: TPWorkoutLibraryItemDTO): Workout {
        return toWorkout(tpWorkout, LocalDate.now())
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

    private fun toWorkout(tpWorkout: TPBaseWorkoutResponseDTO, workoutDate: LocalDate): Workout {
        val workoutsStructure = toWorkoutStructure(tpWorkout)
        var description = tpWorkout.description.orEmpty()
        description += tpWorkout.coachComments?.let { "\n- - - -\n$it" }.orEmpty()

        return Workout(
            workoutDate,
            tpWorkout.getWorkoutType()!!,
            tpWorkout.title.ifBlank { "Workout" },
            description,
            tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
            tpWorkout.tssPlanned,
            workoutsStructure,
            getWorkoutExternalData(tpWorkout)
        )
    }

    private fun getWorkoutExternalData(tpWorkout: TPBaseWorkoutResponseDTO): ExternalData {
        return ExternalData
            .empty()
            .withTrainingPeaks(tpWorkout.id)
            .withSimpleString(tpWorkout.description ?: "")
    }

    private fun toWorkoutStructure(tpWorkout: TPBaseWorkoutResponseDTO): WorkoutStructure? {
        if (tpWorkout.structure == null || tpWorkout.structure.structure.isEmpty()) {
            return null
        }

        try {
            val steps = TPStructureToStepMapper(tpWorkout.structure).mapToWorkoutSteps()
            return WorkoutStructure(
                tpWorkout.structure.toTargetUnit(),
                steps
            )
        } catch (e: PlatformException) {
            log.warn("Can't convert workout - ${tpWorkout.title}, error - ${e.message}")
            return null
        }
    }
}
