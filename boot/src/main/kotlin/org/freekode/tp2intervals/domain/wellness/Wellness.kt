package org.freekode.tp2intervals.domain.wellness

class Wellness(
    val date: String?,
    val type: String?,
    val weight: Double?,
) {
    companion object {
        const val DATE_FORMAT = "yyyy-MM-dd"
    }
}
