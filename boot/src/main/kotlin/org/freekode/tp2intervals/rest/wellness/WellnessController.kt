package org.freekode.tp2intervals.rest.wellness

import org.freekode.tp2intervals.app.wellness.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.app.wellness.CopyWellnessResponse
import org.freekode.tp2intervals.app.wellness.WellnessService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WellnessController(
    private val wellnessService: WellnessService,
) {
    @PostMapping("/api/wellness/copy-calendar-to-calendar")
    fun copyWellnessFromCalendarToCalendar(@RequestBody request: CopyFromCalendarToCalendarRequest): CopyWellnessResponse {
        return wellnessService.copyWellnessC2C(request)
    }
}
