package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import java.time.LocalDate

interface WorkoutRepository {
    fun platform(): Platform

    fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout>

    fun getWorkout(id: String): Workout

    fun planWorkout(workout: Workout)

    fun saveWorkout(workout: Workout, plan: Plan)
}
