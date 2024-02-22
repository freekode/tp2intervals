package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.domain.TrainingType

open class WorkoutsBaseRequestDTO(
    val types: List<TrainingType>,
    val startDate: String,
    val endDate: String,
    val skipSynced: Boolean = false
)
