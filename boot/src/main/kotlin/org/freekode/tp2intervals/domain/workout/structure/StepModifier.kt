package org.freekode.tp2intervals.domain.workout.structure

enum class StepModifier(val value: String) {
    NONE(""),
    POWER_INSTANT("power=1s"),
    POWER_3S("power=3s"),
    POWER_10S("power=10s"),
    POWER_30S("power=30s");
}
