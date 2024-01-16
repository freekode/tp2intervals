package org.freekode.tp2intervals.domain.plan

import java.time.LocalDate

data class Plan(
    val id: PlanId,
    val startDate: LocalDate
) {
    companion object {
        fun empty() = Plan(PlanId("empty"), LocalDate.now())
    }
}
