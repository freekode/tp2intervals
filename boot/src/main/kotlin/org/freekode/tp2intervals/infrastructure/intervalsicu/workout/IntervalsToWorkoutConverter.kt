package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.WorkoutStep

class IntervalsToWorkoutConverter(
    private val eventDTO: IntervalsEventDTO
) {
    private val zones: List<IntervalsWorkoutDocDTO.IntervalsWorkoutZoneDTO>? = eventDTO.workout_doc?.zoneTimes

    fun toWorkout(): Workout {
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
        val targetMapper = IntervalsWorkoutTargetMapper(zones)
        val mainTarget = targetMapper.mapMainTarget(stepDTO)
        val cadenceTarget = targetMapper.mapCadenceTarget(stepDTO.cadence)
        val intensity = mapIntensityType(stepDTO)

        return WorkoutSingleStep(
            stepDTO.text ?: "Step",
            stepDTO.duration?.let { Duration.ofSeconds(it) } ?: Duration.ZERO,
            mainTarget,
            cadenceTarget,
            intensity,
            stepDTO.ramp == true
        )
    }

    private fun mapIntensityType(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): StepIntensityType {
        return if (true == stepDTO.warmup) {
            StepIntensityType.WARM_UP
        } else if (true == stepDTO.cooldown) {
            StepIntensityType.COOL_DOWN
        } else {
            StepIntensityType.default()
        }
    }
}
