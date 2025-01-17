package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.workout.Attachment
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

    fun toWorkout(tpWorkout: TPWorkoutCalendarResponseDTO, attachments: List<Attachment> = listOf()): Workout {
        return toWorkout(tpWorkout, tpWorkout.workoutDay.toLocalDate(), attachments)
    }

    fun toWorkout(tpWorkout: TPWorkoutLibraryItemDTO, attachments: List<Attachment> = listOf()): Workout {
        return toWorkout(tpWorkout, LocalDate.now(), attachments)
    }

    fun toWorkout(tpNote: TPNoteResponseDTO): Workout {
        return Workout.note(
            tpNote.noteDate.toLocalDate(),
            tpNote.title,
            tpNote.description,
            ExternalData.empty().withTrainingPeaks(tpNote.id.toString())
        )
    }

    private fun toWorkout(tpWorkout: TPBaseWorkoutResponseDTO, workoutDate: LocalDate, attachments: List<Attachment>): Workout {
        val workoutsStructure = toWorkoutStructure(tpWorkout)

        var description = tpWorkout.description.orEmpty()
        description += tpWorkout.coachComments?.let { "\n- - - -\n$it" }.orEmpty()

        return Workout(
            WorkoutDetails(
                tpWorkout.getWorkoutType()!!,
                if (tpWorkout.title.isNullOrBlank()) "Workout" else tpWorkout.title,
                description,
                tpWorkout.totalTimePlanned?.let { Duration.ofMinutes((it * 60).toLong()) },
                tpWorkout.tssPlanned,
                getWorkoutExternalData(tpWorkout),
                attachments
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
            log.warn("Error during TP Workout conversion, skipping, id: ${tpWorkout.id}, name: ${tpWorkout.title}, error - ${e.message}'")
            null
        }

    private fun getWorkoutExternalData(tpWorkout: TPBaseWorkoutResponseDTO): ExternalData {
        return ExternalData.empty().withTrainingPeaks(tpWorkout.id).fromSimpleString(tpWorkout.description ?: "")
    }
}
