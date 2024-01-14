package org.freekode.tp2intervals.domain.config

data class AppConfig(
    val tpConfig: TrainingPeaksConfig,
    val intervalsConfig: IntervalsConfig,
    val rampStepDuration: Int = 60
)
