package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import java.time.LocalDate

data class CopyWorkoutsResponse(
    val copied: Int,
    val filteredOut: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val externalData: ExternalData,
    val alreadySynced: Int = 0,
    val removed: Int = 0,
    val skipped: Int = 0,
    val updated: Int = 0,
    val previewActions: List<SyncPlanAction> = emptyList(),
)
