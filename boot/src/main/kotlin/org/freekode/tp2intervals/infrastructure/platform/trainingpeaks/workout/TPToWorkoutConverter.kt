package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryItemDTO
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.FromTPStructureConverter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate

@Component
class TPToWorkoutConverter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun toWorkout(tpWorkout: TPWorkoutCalendarResponseDTO): Workout {
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
            ExternalData.empty().withTrainingPeaks(tpNote.id.toString())
        )
    }

    private fun toWorkout(tpWorkout: TPBaseWorkoutResponseDTO, workoutDate: LocalDate): Workout {
        val workoutsStructure = toWorkoutStructure(tpWorkout)

        var description = tpWorkout.description.orEmpty()
        description += tpWorkout.coachComments?.let { "\n- - - -\n$it" }.orEmpty()

        return Workout(
            WorkoutDetails(
                tpWorkout.getWorkoutType()!!,
                tpWorkout.title.ifBlank { "Workout" },
                description,
                tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
                tpWorkout.tssPlanned,
                getWorkoutExternalData(tpWorkout)
            ),
            workoutDate,
            workoutsStructure,
        )
    }

    private fun toWorkoutStructure(tpWorkout: TPBaseWorkoutResponseDTO) =
        try {
            if (tpWorkout.structure == null || tpWorkout.structure.structure.isEmpty()) {
                throw IllegalArgumentException("There is no structure")
            }
            FromTPStructureConverter.toWorkoutStructure(tpWorkout.structure)
        } catch (e: IllegalArgumentException) {
            log.warn("Can't convert workout - ${tpWorkout.title}, error - ${e.message}'")
            null
        }

    private fun getWorkoutExternalData(tpWorkout: TPBaseWorkoutResponseDTO): ExternalData {
        return ExternalData.empty().withTrainingPeaks(tpWorkout.id).withSimpleString(tpWorkout.description ?: "")
    }
}
