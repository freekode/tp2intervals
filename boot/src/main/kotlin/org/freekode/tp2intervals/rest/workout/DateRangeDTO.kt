package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.domain.TrainingType

class DateRangeDTO(
    val types: List<TrainingType>,
    val startDate: String,
    val endDate: String,
)
