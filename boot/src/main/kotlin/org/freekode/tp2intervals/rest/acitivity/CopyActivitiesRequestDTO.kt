package org.freekode.tp2intervals.rest.acitivity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType

class CopyActivitiesRequestDTO(
    val startDate: String,
    val endDate: String,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
