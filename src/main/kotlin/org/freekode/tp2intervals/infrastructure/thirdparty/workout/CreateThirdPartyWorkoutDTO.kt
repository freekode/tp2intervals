package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import java.time.LocalDate

class CreateThirdPartyWorkoutDTO(
    var athleteId: String,
    var workoutDay: LocalDate,
    var workoutTypeValueId: Int,
    var title: String,
    var totalTimePlanned: Double?,
    var tssPlanned: Double?,
    var structure: String?
)
