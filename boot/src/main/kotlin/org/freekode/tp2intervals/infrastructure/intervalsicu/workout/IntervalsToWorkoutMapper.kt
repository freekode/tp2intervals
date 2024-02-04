package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget

class IntervalsToWorkoutMapper(
    private val eventDTO: IntervalsEventDTO
) {
    private val intervalsWorkoutTargetMapper = eventDTO.workout_doc?.zoneTimes?.let { IntervalsWorkoutTargetMapper(it) }

    fun mapToWorkout(): Workout {
        return Workout(
            eventDTO.start_date_local.toLocalDate(),
            eventDTO.mapType(),
            eventDTO.name,
            eventDTO.description,
            eventDTO.mapDuration(),
            eventDTO.icu_training_load?.toDouble(),
            eventDTO.workout_doc?.let { mapToWorkoutSteps(it) } ?: listOf(),
            WorkoutExternalData.intervals(eventDTO.id.toString())
        )
    }

    private fun mapToWorkoutSteps(workoutDoc: IntervalsWorkoutDocDTO): List<WorkoutStep> {
        return workoutDoc.steps.map {
            if (it.reps != null) {
                mapMultiStep(it)
            } else {
                mapSingleStep(it)
            }
        }
    }

    private fun mapMultiStep(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutMultiStep {
        return WorkoutMultiStep(
            stepDTO.text ?: "Step",
            stepDTO.reps!!,
            stepDTO.steps!!.map { mapSingleStep(it) }
        )
    }

    private fun mapSingleStep(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutSingleStep {
        val targets = workoutStepTargets(stepDTO)
        val intensity = intensityType(stepDTO)

        return WorkoutSingleStep(
            stepDTO.text ?: "Step",
            stepDTO.duration?.let { Duration.ofSeconds(it) } ?: Duration.ZERO,
            targets.first,
            targets.second,
            intensity,
            stepDTO.ramp == true
        )
    }

    private fun intensityType(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): StepIntensityType {
        return if (true == stepDTO.warmup) {
            StepIntensityType.WARM_UP
        } else if (true == stepDTO.cooldown) {
            StepIntensityType.COOL_DOWN
        } else {
            StepIntensityType.default()
        }
    }

    private fun workoutStepTargets(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): Pair<WorkoutStepTarget, WorkoutStepTarget?> {
        if (intervalsWorkoutTargetMapper == null) {
            throw RuntimeException("Can't map step targets without mapper, probably zoneTimes are empty")
        }

        val mainTarget = if (stepDTO.power != null) {
            intervalsWorkoutTargetMapper.map(stepDTO.power)
        } else if (stepDTO.hr != null) {
            intervalsWorkoutTargetMapper.map(stepDTO.hr)
        } else {
            throw RuntimeException("wtf")
        }
        val cadenceTarget = stepDTO.cadence?.let { intervalsWorkoutTargetMapper.map(it) }
        return mainTarget to cadenceTarget
    }
}
