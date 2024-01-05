package org.freekode.tp2intervals.app

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.intervalsicu.folder.IntervalsFolderRepository
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsWorkoutRepository
import org.freekode.tp2intervals.infrastructure.trainingpeaks.workout.TrainingPeaksWorkoutRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MainService(
    private val trainingPeaksWorkoutRepository: TrainingPeaksWorkoutRepository,
    private val intervalsFolderRepository: IntervalsFolderRepository,
    private val intervalsWorkoutRepository: IntervalsWorkoutRepository,
    private val connectionTesters: List<ConnectionTester>,
) {

    fun copyPlanFromThirdParty(startDate: LocalDate, endDate: LocalDate) {
        val workouts = trainingPeaksWorkoutRepository.getWorkouts(startDate, endDate)
        val plan = intervalsFolderRepository.createPlan("My Plan - $startDate", startDate)
        workouts.forEach { intervalsWorkoutRepository.createAndPlanWorkout(plan, it) }
    }

    fun copyRunWeightActivitiesFromIntervals(date: LocalDate) {
        val workouts = intervalsWorkoutRepository.getActivities(date, date)
        workouts
            .filter { it.type == TrainingType.WEIGHT || it.type == TrainingType.RUN }
            .forEach { trainingPeaksWorkoutRepository.createActivity(it) }
    }

    fun planTodayAndTomorrowWorkouts() {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(1)

        val workouts = intervalsWorkoutRepository.getPlannedWorkouts(startDate, endDate)
        workouts.forEach { trainingPeaksWorkoutRepository.planWorkout(it) }
    }

    fun testConnections() {
        connectionTesters.forEach { it.test() }
    }
}
