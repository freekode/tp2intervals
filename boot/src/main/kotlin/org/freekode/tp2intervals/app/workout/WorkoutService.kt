package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.schedule.SchedulerService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.jobrunr.jobs.RecurringJob
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WorkoutService(
    private val workoutRepositoryStrategy: WorkoutRepositoryStrategy,
    private val planRepositoryStrategy: PlanRepositoryStrategy,
    private val schedulerService: SchedulerService,
    @Value("\${app.plan-workouts-cron}") private val planWorkoutsCron: String
) {
    fun copyPlan(request: CopyPlanRequest) {
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)
        val targetPlanRepository = planRepositoryStrategy.getRepository(request.targetPlatform)

        val workouts = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        val plan = targetPlanRepository.createPlan("My Plan - ${request.startDate}", request.startDate)
        workouts.forEach { targetWorkoutRepository.planWorkout(it, plan) }
    }

    fun planWorkouts(request: PlanWorkoutsRequest) {
        val sourceWorkoutRepository = workoutRepositoryStrategy.getRepository(request.sourcePlatform)
        val targetWorkoutRepository = workoutRepositoryStrategy.getRepository(request.targetPlatform)

        val workouts = sourceWorkoutRepository.getPlannedWorkouts(request.startDate, request.endDate)
        workouts.forEach { targetWorkoutRepository.planWorkout(it, Plan.empty()) }
    }

    fun schedulePlanWorkoutsJob(request: PlanWorkoutsRequest) {
        val jobId = getJobId(request.sourcePlatform, request.targetPlatform)
        schedulerService.startJob(jobId, planWorkoutsCron) { planWorkouts(request) }
    }

    fun stopJob(sourcePlatform: Platform, targetPlatform: Platform) {
        schedulerService.stopJob(getJobId(sourcePlatform, targetPlatform))
    }

    fun getJob(sourcePlatform: Platform, targetPlatform: Platform): RecurringJob? {
        return schedulerService.getJob(getJobId(sourcePlatform, targetPlatform))
    }

    private fun getJobId(sourcePlatform: Platform, targetPlatform: Platform): String {
        return "plan-workouts-${sourcePlatform}-${targetPlatform}-job"
    }
}
