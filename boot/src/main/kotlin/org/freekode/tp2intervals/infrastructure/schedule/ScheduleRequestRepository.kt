package org.freekode.tp2intervals.infrastructure.schedule

import org.springframework.data.repository.CrudRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ScheduleRequestRepository : CrudRepository<ScheduleRequestEntity, Int> {
    fun findByRequestJson(requestJson: String): ScheduleRequestEntity?

    @Modifying
    @Transactional
    @Query("update ScheduleRequestEntity s set s.lastResultJson = :result where s.id = :id")
    fun updateLastResult(id: Int, result: String): Int
}
