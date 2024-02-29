package org.freekode.tp2intervals.app.plan

import java.time.LocalDate

data class CopyPlanResponse(
    val copiedWorkouts: Int,
    val startDate: LocalDate,
)
