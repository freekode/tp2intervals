package org.freekode.tp2intervals.domain.workout

import java.io.Serializable
import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

data class Workout(
    val details: WorkoutDetails,
    val date: LocalDate?,
    val structure: WorkoutStructure?,
) : Serializable {
    constructor(
        date: LocalDate, type: TrainingType, name: String, description: String?,
        duration: Duration?, load: Int?, structure: WorkoutStructure?, externalData: ExternalData
    ) : this(WorkoutDetails(type, name, description, duration, load, externalData), date, structure)

    companion object {
        fun note(date: LocalDate, title: String, description: String?, externalData: ExternalData): Workout {
            return Workout(date, TrainingType.NOTE, title, description, null, null, null, externalData)
        }
    }

    fun withDate(date: LocalDate): Workout {
        return Workout(date, details.type, details.name, details.description, details.duration, details.load, structure, details.externalData)
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
