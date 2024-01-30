package org.freekode.tp2intervals.infrastructure.schedule

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "schedule_requests")
@Entity
data class ScheduleRequestEntity(
    @Id
    @GeneratedValue
    val id: Long?,

    @Column
    var requestJson: String?,
) {
    constructor() : this(null, null)
}
