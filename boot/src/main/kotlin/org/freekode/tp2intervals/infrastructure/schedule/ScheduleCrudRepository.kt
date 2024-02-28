package org.freekode.tp2intervals.infrastructure.schedule

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleCrudRepository : CrudRepository<ScheduleRequestEntity, Long> {
    fun findByClassName(className: String): List<ScheduleRequestEntity>
}
