package org.freekode.tp2intervals.domain.workout.structure

enum class StepIntensityType {
    WARM_UP,
    ACTIVE,
    REST,
    COOL_DOWN;

    companion object {
        fun default() = ACTIVE
    }
}
