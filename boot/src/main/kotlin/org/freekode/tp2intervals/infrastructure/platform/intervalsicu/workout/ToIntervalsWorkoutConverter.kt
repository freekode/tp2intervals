package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.Signature
import org.freekode.tp2intervals.infrastructure.utils.Date
import java.time.LocalDate

class ToIntervalsWorkoutConverter {
    private val unwantedStepRegex = "^[-*]".toRegex(RegexOption.MULTILINE)

    fun createWorkoutRequestDTO(libraryContainer: LibraryContainer, workout: Workout): CreateWorkoutRequestDTO {
        val workoutString = getWorkoutString(workout)
        var description = getDescription(workout, workoutString)
        val name: String
        if (workout.details.name.length > 80) {
            name = workout.details.name.substring(0, 76).trim() + "..."
            description = "Name: ${workout.details.name}\n" + description
        } else {
            name = workout.details.name
        }
        val request = CreateWorkoutRequestDTO(
            libraryContainer.externalData.intervalsId.toString(),
            Date.daysDiff(libraryContainer.startDate, workout.date ?: LocalDate.now()),
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            name,
            workout.details.duration?.seconds,
            workout.details.load,
            description,
            null,
        )
        return request
    }

    fun createEventRequestDTO(workout: Workout): CreateEventRequestDTO {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)
        return CreateEventRequestDTO(
            (workout.date ?: LocalDate.now()).atStartOfDay().toString(),
            workout.details.name,
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            IntervalsTrainingTypeMapper.getByIntervalsType(workout.details.type.toString()).category.toString(),
            description,
            workout.details.duration?.seconds,
            workout.details.load,
        )
    }

    private fun getDescription(workout: Workout, workoutString: String?): String {
        return buildString {
            // Block - Description
            workout.details.description?.takeIf { it.isNotBlank() }?.let {
                val formatted = it.replace(unwantedStepRegex, "`-")
                append("\n- - - -\n${Signature.description}")
                append("#### Description\n\n$formatted\n\n<br>\n\n")
            }

            // Block - Workout Details
            workoutString?.takeIf { it.isNotBlank() }?.let {
                append("\n\n- - - -\n")
                append("#### Workout Details\n$it\n\n")
            }

            // Block - ExternalData (Signature/Links)
            val externalData = workout.details.externalData.toSimpleString()
            if (externalData.isNotBlank()) {
                append(externalData)
            }
        }.trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun getWorkoutString(workout: Workout) =
        if (workout.structure != null) {
            ToIntervalsStructureConverter(workout.structure).toIntervalsStructureStr()
        } else {
            null
        }
}
