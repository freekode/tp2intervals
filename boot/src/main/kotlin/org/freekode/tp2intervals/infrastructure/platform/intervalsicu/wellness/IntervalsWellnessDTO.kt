package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.wellness

import java.time.OffsetDateTime

class IntervalsWellnessDTO(
    val id: String?, // Data in format "yyyy-MM-dd"
    val weight: Double? = -1.0,
    val ctl: Double? = null,
    val atl: Double? = null,
    val rampRate: Double? = null,
    val ctlLoad: Double? = null,
    val atlLoad: Double? = null,
    val sportInfo: List<SportInfoDTO>? = null,
    val updated: OffsetDateTime? = null,

    // Métricas de Saúde (Nullables)
    val restingHR: Int? = null,
    val hrv: Double? = null,
    val hrvSDNN: Double? = null,
    val menstrualPhase: String? = null,
    val menstrualPhasePredicted: String? = null,
    val kcalConsumed: Int? = null,
    val sleepSecs: Int? = null,
    val sleepScore: Double? = null,
    val sleepQuality: Int? = null,
    val avgSleepingHR: Double? = null,
    val soreness: Int? = null,
    val fatigue: Int? = null,
    val stress: Int? = null,
    val mood: Int? = null,
    val motivation: Int? = null,
    val injury: Int? = null,
    val spO2: Double? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val hydration: Int? = null,
    val hydrationVolume: Double? = null,
    val readiness: Double? = null,
    val baevskySI: Double? = null,
    val bloodGlucose: Double? = null,
    val lactate: Double? = null,
    val bodyFat: Double? = null,
    val abdomen: Double? = null,
    val vo2max: Double? = null,
    val comments: String? = null,
    val steps: Int? = null,
    val respiration: Double? = null,
    val locked: Boolean? = null,
    val tempWeight: Boolean = false,
    val tempRestingHR: Boolean = false
) {
    data class SportInfoDTO(
        val type: String?,
        val eftp: Double?,
        val wPrime: Double?,
        val pMax: Double?
    )
}