package org.freekode.tp2intervals.app.wellness

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.wellness.WellnessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WellnessService(
    repositories: List<WellnessRepository>,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val repositoryMap = repositories.associateBy { it.platform() }

    fun copyWellnessC2C(request: CopyFromCalendarToCalendarRequest): CopyWellnessResponse {
        log.info("Received request for copy calendar to calendar: $request")
        val sourceRepository = repositoryMap[request.sourcePlatform]!!
        val targetRepository = repositoryMap[request.targetPlatform]!!

        val allToSync = sourceRepository.getFromCalendar(request.startDate, request.endDate)

        val response = CopyWellnessResponse(
            1,
            request.startDate,
            request.endDate,
            ExternalData.empty()
        )
        targetRepository.saveToCalendar(allToSync, request.startDate, request.endDate)
        log.info("Saved Wellness (Metrics) to calendar successfully: $response")
        return response
    }

}