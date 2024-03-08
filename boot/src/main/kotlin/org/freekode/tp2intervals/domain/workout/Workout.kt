package org.freekode.tp2intervals.domain.workout

import java.io.Serializable
import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

data class Workout(
    val date: LocalDate,
    val type: TrainingType,
    val name: String,
    val description: String?,
    val duration: Duration?,
    val load: Int?,
    val structure: WorkoutStructure?,
    val externalData: ExternalData,
) : Serializable {
    companion object {
        fun note(date: LocalDate, title: String, description: String?, externalData: ExternalData): Workout {
            return Workout(date, TrainingType.NOTE, title, description, null, null, null, externalData)
        }
    }

    fun withDate(date: LocalDate): Workout {
        return Workout(date, type, name, description, duration, load, structure, externalData)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Workout

        if (type != other.type) return false
        if (name != other.name) return false
        if (load != other.load) return false
        if (externalData != other.externalData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (load ?: 0)
        result = 31 * result + externalData.hashCode()
        return result
    }


}
