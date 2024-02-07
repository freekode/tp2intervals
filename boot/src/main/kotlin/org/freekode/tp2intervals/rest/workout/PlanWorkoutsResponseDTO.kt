package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate

class PlanWorkoutsResponseDTO(
    val planned: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)
