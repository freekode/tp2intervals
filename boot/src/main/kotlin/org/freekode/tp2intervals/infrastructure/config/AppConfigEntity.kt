package org.freekode.tp2intervals.infrastructure.config

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "config")
@Entity
data class AppConfigEntity(
    @Id val id: Int?,
    val tpAuthCookie: String?,
    val intervalsApiKey: String?,
    val intervalsAthleteId: String?,
) {
    constructor() : this(null, null, null, null)
}
