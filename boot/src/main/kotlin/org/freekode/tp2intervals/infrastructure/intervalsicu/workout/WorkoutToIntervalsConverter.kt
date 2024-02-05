package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.springframework.stereotype.Component

@Component
class WorkoutToIntervalsConverter {
    fun mapToIntervalsWorkout(workout: Workout): String? {
        if (workout.steps.isEmpty()) {
            return null
        }
        return workout.steps.joinToString(separator = "\n") { mapToIntervalsStep(it) }
    }

    private fun mapToIntervalsStep(workoutStep: WorkoutStep): String {
        return if (workoutStep.isSingleStep()) {
            mapSingleStep(workoutStep as WorkoutSingleStep).getStepString()
        } else {
            "\n" + mapMultiStep(workoutStep as WorkoutMultiStep) + "\n"
        }
    }

    private fun mapSingleStep(workoutStep: WorkoutSingleStep): IntervalsWorkoutStep {
        val target = workoutStep.target
        val cadence = workoutStep.cadence
        return IntervalsWorkoutStep(
            workoutStep.title,
            workoutStep.duration,
            target.start.toString(),
            target.end.toString(),
            IntervalsWorkoutStep.TargetType.findByTargetUnit(target.unit),
            cadence?.start,
            cadence?.end
        )
    }

    private fun mapMultiStep(workoutMultiStep: WorkoutMultiStep): String {
        val steps = listOf("${workoutMultiStep.repetitions}x") + workoutMultiStep.steps.map { mapToIntervalsStep(it) }
        return steps.joinToString(separator = "\n")
    }
}
