package org.freekode.tp2intervals.domain.workout.structure

import java.io.Serializable

interface WorkoutStep : Serializable {
    fun isSingleStep(): Boolean
}
