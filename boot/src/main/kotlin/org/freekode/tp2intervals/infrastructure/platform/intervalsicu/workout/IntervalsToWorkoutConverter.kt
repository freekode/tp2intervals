package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import java.time.Duration
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.ExternalData
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
            ExternalData.empty().withIntervals(eventDTO.id.toString())
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

        return WorkoutSingleStep(
            stepDTO.text ?: "Step",
            stepDTO.duration?.let { Duration.ofSeconds(it) } ?: Duration.ofMinutes(10),
            mainTarget,
            cadenceTarget,
            stepDTO.ramp == true
        )
    }
}
