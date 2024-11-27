package org.freekode.tp2intervals.domain

enum class TrainingType(val title: String) {
    BIKE("Ride"),
    MTB("MTB"),
    VIRTUAL_BIKE("Virtual Ride"),
    RUN("Run"),
    SWIM("Swim"),
    WALK("Walk"),
    WEIGHT("Weight"),
    NOTE("Note"),
    UNKNOWN("Unknown");

    companion object {
        val DEFAULT_LIST = listOf(BIKE, VIRTUAL_BIKE, MTB, RUN, SWIM)
    }
}
