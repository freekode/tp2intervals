package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.StepIntensityType
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.WorkoutStep
import org.freekode.tp2intervals.domain.workout.WorkoutStepTarget
import org.freekode.tp2intervals.infrastructure.intervalsicu.IntervalsActivityDTO
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IntervalsWorkoutMapper {
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

    private fun mapMultiStep(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutStep {
        return WorkoutStep(
            stepDTO.text ?: "Step",
            stepDTO.reps!!,
            stepDTO.duration?.let { Duration.ofSeconds(it) } ?: Duration.ZERO,
            listOf(),
            StepIntensityType.default(),
            stepDTO.steps!!.map { mapSingleStep(it) }
        )
    }

    private fun mapSingleStep(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): WorkoutStep {
        val targets = workoutStepTargets(stepDTO)
        val intensity = intensityType(stepDTO)

        return WorkoutStep(
            stepDTO.text ?: "Step",
            1,
            stepDTO.duration?.let { Duration.ofSeconds(it) } ?: Duration.ZERO,
            targets,
            intensity,
            listOf()
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

    private fun workoutStepTargets(stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO): MutableList<WorkoutStepTarget> {
        val targets = mutableListOf<WorkoutStepTarget>()
        addTargetIfExists(stepDTO.power, targets)
        addTargetIfExists(stepDTO.cadence, targets)
        addTargetIfExists(stepDTO.hr, targets)
        return targets
    }

    private fun addTargetIfExists(
        stepValue: IntervalsWorkoutDocDTO.StepValueDTO?,
        targets: MutableList<WorkoutStepTarget>
    ) {
        if (stepValue == null) {
            return
        }

        val (min, max) = mapTargetValue(stepValue)
        WorkoutStepTarget(mapTargetUnit(stepValue.units), min, max).also { targets.add(it) }
    }

    private fun mapTargetUnit(units: String): WorkoutStepTarget.TargetUnit =
        when (units) {
            "%ftp" -> WorkoutStepTarget.TargetUnit.FTP_PERCENTAGE
            "%lthr" -> WorkoutStepTarget.TargetUnit.LTHR_PERCENTAGE
            "rpm" -> WorkoutStepTarget.TargetUnit.RPM
            else -> throw RuntimeException("unknown unit $units")
        }

    private fun mapTargetValue(stepValueDTO: IntervalsWorkoutDocDTO.StepValueDTO): Pair<Int, Int> =
        if (stepValueDTO.value != null) {
            Pair(stepValueDTO.value, stepValueDTO.value)
        } else {
            Pair(stepValueDTO.start!!, stepValueDTO.end!!)
        }
}
