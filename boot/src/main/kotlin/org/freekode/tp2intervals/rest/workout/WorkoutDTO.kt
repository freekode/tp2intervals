package org.freekode.tp2intervals.rest.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.ExternalData

class WorkoutDTO(
    val name: String,
    val duration: Duration?,
    val load: Int?,
    val externalData: ExternalData,
)
