package org.freekode.tp2intervals.domain.plan

import java.io.Serializable
import java.time.LocalDate
import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.infrastructure.utils.Date

data class Plan(
    val name: String,
    val startDate: LocalDate,
    val externalData: ExternalData,
) : Serializable {
    companion object {
        fun fromMonday(name: String, externalData: ExternalData): Plan {
            return Plan(name, Date.thisMonday(), externalData)
        }
    }
}
