package org.freekode.tp2intervals.domain.activity

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.utils.Base64
import org.springframework.core.io.Resource
import java.time.LocalDateTime

data class Activity(
    val startedAt: LocalDateTime,
    val type: TrainingType,
    val title: String,
    val resource: String?
) {
    fun withResource(resource: Resource) =
        Activity(startedAt, type, title, Base64.toString(resource))
}
