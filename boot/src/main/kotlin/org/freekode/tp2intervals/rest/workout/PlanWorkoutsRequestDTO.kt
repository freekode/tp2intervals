package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType

class PlanWorkoutsRequestDTO(
    val skipSynced: Boolean = false,
    val types: List<TrainingType>,
    val startDate: String,
    val endDate: String,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
