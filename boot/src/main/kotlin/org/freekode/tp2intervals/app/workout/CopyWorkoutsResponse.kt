package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import java.time.LocalDate

data class CopyWorkoutsResponse(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val externalData: ExternalData
)
