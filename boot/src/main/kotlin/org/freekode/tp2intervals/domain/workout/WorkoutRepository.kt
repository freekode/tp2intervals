package org.freekode.tp2intervals.domain.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan

interface WorkoutRepository {
    fun platform(): Platform

    fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout>

    fun getWorkouts(plan: Plan): List<Workout>

    fun getWorkoutsByName(name: String): List<Workout>

    fun planWorkout(workout: Workout)

    fun saveWorkoutToPlan(workout: Workout, plan: Plan)
}
