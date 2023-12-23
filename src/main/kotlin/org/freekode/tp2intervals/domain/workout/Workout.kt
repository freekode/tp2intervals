package org.freekode.tp2intervals.domain.workout

import java.time.Duration
import java.time.LocalDate

data class Workout(
    val scheduledDate: LocalDate,
    val type: WorkoutType,
    val title: String,
    val duration: Duration?,
    val load: Double?,
    val description: String?,
    val steps: List<WorkoutStep>,
    val externalContent: String?
)
