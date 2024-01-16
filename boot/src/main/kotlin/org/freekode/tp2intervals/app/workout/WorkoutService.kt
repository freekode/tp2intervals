package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.plan.Plan
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class WorkoutService(
    workoutRepositoryStrategy: WorkoutRepositoryStrategy,
    planRepositoryStrategy: PlanRepositoryStrategy,
) {
    private val trainingPeaksWorkoutRepository = workoutRepositoryStrategy.getRepository(Platform.TRAINING_PEAKS)
    private val intervalsWorkoutRepository = workoutRepositoryStrategy.getRepository(Platform.INTERVALS)
    private val intervalsPlanRepository = planRepositoryStrategy.getRepository(Platform.INTERVALS)

    fun copyPlanFromThirdParty(startDate: LocalDate, endDate: LocalDate) {
        val workouts = trainingPeaksWorkoutRepository.getPlannedWorkouts(startDate, endDate)
        val plan = intervalsPlanRepository.createPlan("My Plan - $startDate", startDate)
        workouts.forEach { intervalsWorkoutRepository.planWorkout(it, plan) }
    }

    fun planTodayAndTomorrowWorkouts() {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(1)

        val workouts = intervalsWorkoutRepository.getPlannedWorkouts(startDate, endDate)
        workouts.forEach { trainingPeaksWorkoutRepository.planWorkout(it, Plan.empty()) }
    }
}
