package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate

class CopyWorkoutsResponseDTO(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)
