package org.freekode.tp2intervals.domain.activity

import java.time.Duration
import java.time.LocalDateTime
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.utils.Math

data class Activity(
    val startedAt: LocalDateTime,
    val type: TrainingType,
    val title: String,
    val duration: Duration,
    val load: Long?,
    val resource: String?
) {
    private fun compareDuration(otherDuration: Duration): Boolean {
        val durationVariationPercentage = 5
        return Math.percentageDiff(
            duration.seconds.toDouble(),
            otherDuration.seconds.toDouble()
        ) <= durationVariationPercentage
    }

    override fun toString(): String {
        return "Activity(startedAt=$startedAt, type=$type, title='$title', duration=$duration, load=$load)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Activity

        if (startedAt != other.startedAt) return false
        if (compareDuration(other.duration)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startedAt.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}
