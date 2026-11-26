package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration

data class TrainingPeaksConnectionTestResponse(
    val tokenValid: Boolean,
    val username: String? = null,
    val isAthlete: Boolean? = null,
    val isPremium: Boolean? = null,
)
