package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.domain.ExternalData

class WorkoutDetailsDTO(
    val name: String,
    val duration: Double?,
    val load: Int?,
    val externalData: ExternalData,
)
