package org.freekode.tp2intervals.app.activity

import java.time.LocalDate

data class CopyActivitiesResponse(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
