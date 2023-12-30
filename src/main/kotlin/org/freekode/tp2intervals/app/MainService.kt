package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.IntervalsFolderRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.thirdparty.workout.ThirdPartyWorkoutRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MainService(
    private val thirdPartyWorkoutRepository: ThirdPartyWorkoutRepository,
    private val intervalsFolderRepository: IntervalsFolderRepository,
    private val intervalsWorkoutRepository: IntervalsWorkoutRepository
) {

    fun copyPlanFromThirdParty(startDate: LocalDate, endDate: LocalDate) {
        val workouts = thirdPartyWorkoutRepository.getWorkouts(startDate, endDate)
        val plan = intervalsFolderRepository.createPlan("My Plan - $startDate", startDate)
        for (workout in workouts) {
            intervalsWorkoutRepository.createAndPlanWorkout(plan, workout)
        }
    }

    fun copyRunWeightActivitiesFromIntervals(date: LocalDate) {
        val workouts = intervalsWorkoutRepository.getActivities(date, date)
        workouts.stream()
            .filter { it.type == TrainingType.WEIGHT || it.type == TrainingType.RUN }
            .forEach { thirdPartyWorkoutRepository.createActivity(it) }
    }

    fun planTodayTomorrowWorkouts() {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(1)

        val workouts = intervalsWorkoutRepository.getPlannedWorkouts(startDate, endDate)
        workouts.forEach { thirdPartyWorkoutRepository.planWorkout(it) }
    }
}
