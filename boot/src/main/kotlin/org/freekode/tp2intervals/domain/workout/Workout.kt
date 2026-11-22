package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class Workout(
    val details: WorkoutDetails,
    val date: LocalDate?,
    val structure: WorkoutStructure?,
    val startTime: LocalTime? = null,
) : Serializable {
    companion object {
        fun note(date: LocalDate, name: String, description: String?, externalData: ExternalData): Workout {
            return Workout(WorkoutDetails(TrainingType.NOTE, name, description, null, null, externalData), date, null)
        }
    }

    fun withDate(date: LocalDate): Workout {
        return copy(date = date)
    }

    fun withStartTime(startTime: LocalTime?): Workout {
        return copy(startTime = startTime)
    }

    fun isSameForSync(other: Workout): Boolean {
        if (date != null && other.date != null && date != other.date) return false

        val thisExternal = details.externalData
        val otherExternal = other.details.externalData

        val trainerRoadIds = thisExternal.trainerRoadId to otherExternal.trainerRoadId
        if (trainerRoadIds.first != null && trainerRoadIds.second != null) {
            return trainerRoadIds.first == trainerRoadIds.second
        }

        val trainingPeaksIds = thisExternal.trainingPeaksId to otherExternal.trainingPeaksId
        if (trainingPeaksIds.first != null && trainingPeaksIds.second != null) {
            return trainingPeaksIds.first == trainingPeaksIds.second
        }

        val intervalsIds = thisExternal.intervalsId to otherExternal.intervalsId
        if (intervalsIds.first != null && intervalsIds.second != null) {
            return intervalsIds.first == intervalsIds.second
        }

        return details.name == other.details.name &&
            details.duration == other.details.duration
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Workout

        return details == other.details
    }

    override fun hashCode(): Int {
        return details.hashCode()
    }


}
