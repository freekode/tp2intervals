package org.freekode.tp2intervals.app.workout

import java.time.LocalDate

data class CopyPlannedToLibraryResponse(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)
