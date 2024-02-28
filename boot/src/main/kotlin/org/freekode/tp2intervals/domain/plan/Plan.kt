package org.freekode.tp2intervals.domain.plan

import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData

data class Plan(
    val name: String,
    val startDate: LocalDate,
    val externalData: ExternalData,
)
