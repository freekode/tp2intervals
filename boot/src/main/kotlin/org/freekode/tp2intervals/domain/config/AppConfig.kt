package org.freekode.tp2intervals.domain.config

data class AppConfig(
    val tpConfig: TrainingPeaksConfig?,
    val trConfig: TrainerRoadConfig?,
    val intervalsConfig: IntervalsConfig,
)
