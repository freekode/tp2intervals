package org.freekode.tp2intervals.rest.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform

class DeleteWorkoutRequestDTO(
    val platform: Platform,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
}
