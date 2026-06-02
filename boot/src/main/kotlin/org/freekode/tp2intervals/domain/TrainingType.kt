package org.freekode.tp2intervals.domain

enum class TrainingType(val title: String, val category: CategoryType) {
    SWIM("Swim",                    CategoryType.WORKOUT),
    BIKE("Ride",                    CategoryType.WORKOUT),
    RUN("Run",                      CategoryType.WORKOUT),
    BRICK("Brick",                  CategoryType.NOTE),
    //crosstrain
    RACE("Race",                    CategoryType.WORKOUT),
    DAY_OFF("Day-off",              CategoryType.NOTE),
    STRENGTH("Strength",            CategoryType.WORKOUT),

    MTB("MTB",                      CategoryType.WORKOUT),
    VIRTUAL_BIKE("Virtual Ride",    CategoryType.WORKOUT),
    WALK("Walk",                    CategoryType.WORKOUT),
    NOTE("Note",                    CategoryType.NOTE),
    UNKNOWN("Unknown",              CategoryType.WORKOUT),

    //WELLNESS (Metrics)
    WEIGHT("Weight Wellness",       CategoryType.WELLNESS),
    ;

    companion object {
        val DEFAULT_LIST = listOf(BIKE, VIRTUAL_BIKE, MTB, RUN, SWIM, DAY_OFF, NOTE, BRICK)
    }

}
