package org.freekode.tp2intervals.domain.activity

import org.freekode.tp2intervals.domain.TrainingType
import org.springframework.core.io.Resource
import java.time.Duration
import java.time.LocalDateTime

data class Activity(
    val startedAt: LocalDateTime,
    val type: TrainingType,
    val title: String,
    val duration: Duration?,
    val load: Long?,
    val resource: String?
)
