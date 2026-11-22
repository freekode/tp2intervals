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
        return Workout(details, date, structure, startTime)
    }

    fun withStartTime(startTime: LocalTime?): Workout {
        return Workout(details, date, structure, startTime)
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
