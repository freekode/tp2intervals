package org.freekode.tp2intervals.infrastructure.intervalsicu.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutExternalData
import org.freekode.tp2intervals.domain.workout.structure.StepIntensityType
import org.freekode.tp2intervals.domain.workout.structure.WorkoutMultiStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutSingleStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStep
import org.freekode.tp2intervals.domain.workout.structure.WorkoutStructure

class IntervalsToWorkoutConverter(
    private val eventDTO: IntervalsEventDTO
) {
    private val workoutDoc: IntervalsWorkoutDocDTO? = eventDTO.workout_doc

    fun toWorkout(): Workout {
        val workoutsStructure = toWorkoutStructure()

        return Workout(
            eventDTO.start_date_local.toLocalDate(),
            eventDTO.mapType(),
            eventDTO.name,
            eventDTO.description,
            eventDTO.mapDuration(),
            eventDTO.icu_training_load,
            workoutsStructure,
            WorkoutExternalData.intervals(eventDTO.id.toString())
        )
    }

    private fun toWorkoutStructure(): WorkoutStructure? {
        return if (workoutDoc != null && workoutDoc.steps.isNotEmpty()) {
            WorkoutStructure(
                workoutDoc.mapTarget(),
                mapToWorkoutSteps(workoutDoc)
            )
        } else null
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

    private fun mapMultiStep(
        stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO
    ): WorkoutMultiStep {
        return WorkoutMultiStep(
            stepDTO.text ?: "Step",
            stepDTO.reps!!,
            stepDTO.steps!!.map { mapSingleStep(it) }
        )
    }

    private fun mapSingleStep(
        stepDTO: IntervalsWorkoutDocDTO.WorkoutStepDTO
    ): WorkoutSingleStep {
        val targetMapper = IntervalsToTargetConverter(
            workoutDoc!!.ftp?.toDouble(),
            workoutDoc.lthr?.toDouble(),
            workoutDoc.threshold_pace?.toDouble()
        )
        val mainTarget = targetMapper.toMainTarget(stepDTO)
        val cadenceTarget = stepDTO.cadence?.let { targetMapper.toCadenceTarget(it) }
        val intensity = mapIntensityType(stepDTO)

        return WorkoutSingleStep(
            stepDTO.text ?: "Step",
            stepDTO.duration?.let { Duration.ofSeconds(it) } ?: Duration.ofMinutes(10),
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
