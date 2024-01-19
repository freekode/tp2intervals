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
    fun isSame(other: Activity): Boolean {
        return startedAt == other.startedAt && compareDuration(other)
    }

    private fun compareDuration(other: Activity): Boolean {
        val durationVariationPercentage = 5
        return Math.percentageDiff(
            duration.seconds.toDouble(),
            other.duration.seconds.toDouble()
        ) <= durationVariationPercentage
    }

    override fun toString(): String {
        return "Activity(startedAt=$startedAt, type=$type, title='$title', duration=$duration, load=$load)"
    }
}
