package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsFolderRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsWorkoutRepository
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

    fun copyWorkoutsFromIntervals(date: LocalDate) {

    }
}
