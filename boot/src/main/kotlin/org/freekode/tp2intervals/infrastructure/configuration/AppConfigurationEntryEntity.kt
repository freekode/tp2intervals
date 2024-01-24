package org.freekode.tp2intervals.infrastructure.configuration

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "config")
@Entity
data class AppConfigurationEntryEntity(
    @Id
    val key: String?,

    @Column
    var value: String?,
) {
    constructor() : this(null, null)
}
