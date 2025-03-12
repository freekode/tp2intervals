package org.freekode.tp2intervals.infrastructure.schedule

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleRequestRepository : CrudRepository<ScheduleRequestEntity, Int> {
    fun findByRequestJson(requestJson: String): ScheduleRequestEntity?
}
