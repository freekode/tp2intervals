package org.freekode.tp2intervals.app.workout

import java.time.LocalDate

data class PlanWorkoutsResponse(
    val planned: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)
