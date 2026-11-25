package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.PlatformException
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
        validateTrainerRoadMarker(workout, description)
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
        validateTrainerRoadMarker(workout, description)
        return CreateEventRequestDTO(
            if (workout.startTime != null) {
                Date.toIsoLocalDateTime(workout.date ?: LocalDate.now(), workout.startTime)
            } else {
                Date.toIsoLocalDateTime(workout.date ?: LocalDate.now())
            },
            workout.details.name,
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            "WORKOUT",
            description
        )
    }

    private fun getDescription(workout: Workout, workoutString: String?): String {
        var description = workout.details.description
            .orEmpty()
            .replace(unwantedStepRegex, "`-")
            .let { "$it\n- - - -\n${Signature.description}" }
        description += workoutString
            ?.let { "\n\n- - - -\n$it" }
            .orEmpty()
        description += "\n\n${workout.details.externalData.toSimpleString()}"
        return description
    }

    private fun validateTrainerRoadMarker(workout: Workout, description: String) {
        if (workout.details.externalData.trainerRoadId != null &&
            !workout.details.externalData.hasClearTrainerRoadId(description)) {
            throw PlatformException(Platform.INTERVALS, "Cannot sync a workout whose description conflicts with the TrainerRoad ID marker")
        }
    }

    private fun getWorkoutString(workout: Workout) =
        if (workout.structure != null) {
            ToIntervalsStructureConverter(workout.structure).toIntervalsStructureStr()
        } else {
            null
        }
}
