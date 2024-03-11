package org.freekode.tp2intervals.app.workout

import java.time.LocalDate

data class CopyPlannedWorkoutsResponse(
    val planned: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)
