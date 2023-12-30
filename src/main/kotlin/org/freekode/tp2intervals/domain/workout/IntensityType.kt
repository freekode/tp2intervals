package org.freekode.tp2intervals.domain.workout

enum class IntensityType {
    WARM_UP,
    ACTIVE,
    REST,
    COOL_DOWN;

    companion object {
        fun default() = ACTIVE
    }
}
