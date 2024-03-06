package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan

data class CopyPlanRequest(
    val plan: Plan,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
