package org.freekode.tp2intervals.app.wellness

import org.freekode.tp2intervals.domain.ExternalData
import java.time.LocalDate

data class CopyWellnessResponse(
    val copied: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val externalData: ExternalData
)
