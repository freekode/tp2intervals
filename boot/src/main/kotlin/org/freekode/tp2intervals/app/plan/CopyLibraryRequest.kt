package org.freekode.tp2intervals.app.plan

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan

data class CopyLibraryRequest(
    val plan: Plan,
    val newName: String,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
