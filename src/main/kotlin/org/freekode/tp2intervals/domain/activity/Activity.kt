package org.freekode.tp2intervals.domain.activity

import org.freekode.tp2intervals.domain.TrainingType
import java.time.Duration
import java.time.LocalDate

data class Activity(
    val date: LocalDate,
    val type: TrainingType,
    val title: String,
    val duration: Duration?,
    val load: Double?,
)
