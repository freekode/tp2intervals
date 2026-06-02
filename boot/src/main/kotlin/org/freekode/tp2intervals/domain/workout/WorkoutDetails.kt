package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.TrainingType
import java.io.Serializable
import java.time.Duration
import java.time.temporal.ChronoUnit

data class WorkoutDetails(
    val type: TrainingType,
    val name: String,
    val description: String?,
    val duration: Duration?,
    val load: Int?,
    val externalData: ExternalData,
    val attachments: List<Attachment> = listOf(),
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkoutDetails

        if (name.uppercase() != other.name.uppercase()
            && !name.uppercase().startsWith(other.name.uppercase())) return false
        if (duration?.truncatedTo(ChronoUnit.MINUTES) != other.duration?.truncatedTo(ChronoUnit.MINUTES)) return false
        if (externalData != other.externalData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + externalData.hashCode()
        return result
    }
}
