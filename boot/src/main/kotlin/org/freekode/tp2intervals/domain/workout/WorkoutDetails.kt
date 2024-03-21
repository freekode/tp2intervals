package org.freekode.tp2intervals.domain.workout

import java.io.Serializable
import java.time.Duration
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType

data class WorkoutDetails(
    val type: TrainingType,
    val name: String,
    val description: String?,
    val duration: Duration?,
    val load: Int?,
    val externalData: ExternalData,
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkoutDetails

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
