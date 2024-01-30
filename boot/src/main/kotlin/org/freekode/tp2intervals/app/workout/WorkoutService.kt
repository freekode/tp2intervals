package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.app.schedule.SchedulerService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.plan.Plan
import org.springframework.stereotype.Service

@Service
class WorkoutService(
    private val workoutRepositoryStrategy: WorkoutRepositoryStrategy,
    private val planRepositoryStrategy: PlanRepositoryStrategy,
    private val schedulerService: SchedulerService,
    private val appConfigurationRepository: AppConfigurationRepository
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
        val planWorkoutsCron = appConfigurationRepository.getConfiguration("generic.plan-workouts-cron")!!
        val jobId = getJobId(request.sourcePlatform, request.targetPlatform)
        schedulerService.startJob(jobId, planWorkoutsCron) { planWorkouts(request) }
    }

    fun stopJob(sourcePlatform: Platform, targetPlatform: Platform) {
        schedulerService.stopJob(getJobId(sourcePlatform, targetPlatform))
    }

    fun getJob(sourcePlatform: Platform, targetPlatform: Platform): String? {
        return schedulerService.getJob(getJobId(sourcePlatform, targetPlatform))
    }

    private fun getJobId(sourcePlatform: Platform, targetPlatform: Platform): String {
        return "plan-workouts-${sourcePlatform}-${targetPlatform}-job"
    }
}
