package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsActivityDTO
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IntervalsToWorkoutMapper {
    fun mapToWorkout(eventDTO: IntervalsEventDTO): Workout {
        return Workout(
            eventDTO.start_date_local.toLocalDate(),
            eventDTO.type!!.trainingType,
            eventDTO.name,
            eventDTO.description,
            eventDTO.moving_time?.let { Duration.ofSeconds(it) },
            eventDTO.icu_training_load?.toDouble(),
            eventDTO.workout_doc?.let { mapToWorkoutSteps(it) } ?: listOf(),
            WorkoutExternalData.intervals(eventDTO.id.toString())
        )
    }

    fun mapToActivity(eventDTO: IntervalsActivityDTO): Activity {
        return Activity(
            eventDTO.start_date_local,
            eventDTO.type!!.trainingType,
            eventDTO.name,
            eventDTO.moving_time?.let { Duration.ofSeconds(it) },
            eventDTO.icu_training_load?.toDouble(),
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
        val mainTarget = if (stepDTO.power != null) {
            mapTarget(stepDTO.power)
        } else if (stepDTO.hr != null) {
            mapTarget(stepDTO.hr)
        } else {
            throw RuntimeException("wtf")
        }
        val cadenceTarget = stepDTO.cadence?.let { mapTarget(it) }
        return mainTarget to cadenceTarget
    }

    private fun mapTarget(
        stepValue: IntervalsWorkoutDocDTO.StepValueDTO,
    ): WorkoutStepTarget {
        val (min, max) = mapTargetValue(stepValue)
        return WorkoutStepTarget(stepValue.mapTargetUnit(), min, max)
    }

    private fun mapTargetValue(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): Pair<Int, Int> =
        if (stepValueDTO.value != null) {
            stepValueDTO.value to stepValueDTO.value
        } else {
            stepValueDTO.start!! to stepValueDTO.end!!
        }
}
