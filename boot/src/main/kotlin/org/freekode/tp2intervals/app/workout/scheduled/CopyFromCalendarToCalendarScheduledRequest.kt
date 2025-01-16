package org.freekode.tp2intervals.app.workout.scheduled

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToCalendarScheduledRequest(
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
) : Schedulable {
    constructor(request: CopyFromCalendarToCalendarRequest) : this(
        request.types,
        request.skipSynced,
        request.sourcePlatform,
        request.targetPlatform
    )

    fun toRequest() =
        CopyFromCalendarToCalendarRequest(
            LocalDate.now(),
            LocalDate.now(),
            types,
            skipSynced,
            sourcePlatform,
            targetPlatform
        )
}
