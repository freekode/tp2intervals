package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainer
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.Signature
import org.freekode.tp2intervals.infrastructure.utils.Date
import java.time.LocalDate

class ToIntervalsWorkoutConverter {
    private val unwantedStepRegex = "^[-*]".toRegex(RegexOption.MULTILINE)
    private val metadataBlockRegex =
        """\A(?:\r\n|\n|\r)((?:(?:trainingPeaksId|intervalsId|trainerRoadId)=[^\r\n]*(?:\r\n|\n|\r|$))+)""".toRegex()

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
        var description = descriptionWithoutExistingMarker(workout)
            .replace(unwantedStepRegex, "`-")
            .let { "$it\n- - - -\n${Signature.description}" }
        description += workoutString
            ?.let { "\n\n- - - -\n$it" }
            .orEmpty()
        description += "\n\n${workout.details.externalData.toSimpleString()}"
        return description
    }

    private fun descriptionWithoutExistingMarker(workout: Workout): String {
        val description = workout.details.description.orEmpty()
        if (workout.details.externalData.trainerRoadId == null &&
            ExternalData.trainerRoadIdsInDescription(description).isNotEmpty()) {
            rejectTrainerRoadMarker()
        }
        if (!workout.details.externalData.hasClearTrainerRoadId(description)) return description
        if (ExternalData.hasAmbiguousMetadata(description)) rejectTrainerRoadMarker()

        val parts = description.split(ExternalData.externalDataDescriptionSeparator, limit = 2)
        val metadata = metadataBlockRegex.find(parts[1]) ?: rejectTrainerRoadMarker()
        val fields = metadata.groupValues[1].lines().filter { it.isNotEmpty() }
            .map { it.substringBefore("=") to it.substringAfter("=") }
        if (fields.none { it.first == "trainerRoadId" && it.second == workout.details.externalData.trainerRoadId }) {
            rejectTrainerRoadMarker()
        }
        return parts[0] + "\n" + parts[1].substring(metadata.range.last + 1)
    }

    private fun validateTrainerRoadMarker(workout: Workout, description: String) {
        if (workout.details.externalData.trainerRoadId != null &&
            !workout.details.externalData.hasClearTrainerRoadId(description)) {
            rejectTrainerRoadMarker()
        }
    }

    private fun rejectTrainerRoadMarker(): Nothing =
        throw PlatformException(Platform.INTERVALS, "Cannot sync a workout whose description conflicts with the TrainerRoad ID marker")

    private fun getWorkoutString(workout: Workout) =
        if (workout.structure != null) {
            ToIntervalsStructureConverter(workout.structure).toIntervalsStructureStr()
        } else {
            null
        }
}
