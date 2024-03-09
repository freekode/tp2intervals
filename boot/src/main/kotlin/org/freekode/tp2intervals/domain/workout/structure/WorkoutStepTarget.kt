package org.freekode.tp2intervals.domain.workout.structure

import java.io.Serializable

data class WorkoutStepTarget(
    val start: Int,
    val end: Int
): Serializable
