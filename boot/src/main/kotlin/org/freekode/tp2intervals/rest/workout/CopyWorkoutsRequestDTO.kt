package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.domain.TrainingType

class CopyWorkoutsRequestDTO(
    val name: String,
    types: List<TrainingType>,
    startDate: String,
    endDate: String,
) : WorkoutsBaseRequestDTO(types, startDate, endDate)
