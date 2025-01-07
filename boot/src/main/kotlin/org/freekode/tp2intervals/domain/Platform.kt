package org.freekode.tp2intervals.domain

enum class Platform(
    val title: String,
    val key: String,
) {
    GENERIC("Generic", "generic"),
    INTERVALS("Intervals.icu", "intervals"),
    TRAINING_PEAKS("TrainingPeaks", "training-peaks"),
    TRAINER_ROAD("TrainerRoad", "trainer-road"),
}
