package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.Signature
import org.freekode.tp2intervals.infrastructure.utils.Date

class WorkoutToIntervalsConverter {
    private val unwantedStepRegex = "^-".toRegex(RegexOption.MULTILINE)

    fun createWorkoutRequestDTO(libraryContainer: LibraryContainer, workout: Workout): CreateWorkoutRequestDTO {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)
        val request = CreateWorkoutRequestDTO(
            libraryContainer.externalData.intervalsId.toString(),
            Date.daysDiff(libraryContainer.startDate, workout.date ?: LocalDate.now()),
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            workout.details.name, // "Name is too long"
            workout.details.duration?.seconds,
            workout.details.load,
            description,
            null,
        )
        return request
    }

    private fun getDescription(workout: Workout, workoutString: String?): String {
        var description = workout.details.description
            .orEmpty()
            .replace(unwantedStepRegex, "--")
            .let { "$it\n- - - -\n${Signature.description}" }
        description += workoutString
            ?.let { "\n\n- - - -\n$it" }
            .orEmpty()
        return description
    }

    private fun getWorkoutString(workout: Workout) =
        if (workout.structure != null) {
            StructureToIntervalsConverter(workout.structure).toIntervalsStructureStr()
        } else {
            null
        }
}
